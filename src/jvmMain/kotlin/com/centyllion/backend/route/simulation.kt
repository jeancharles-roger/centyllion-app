package com.centyllion.backend.route

import com.centyllion.backend.data.Data
import com.centyllion.backend.hasReadAccess
import com.centyllion.backend.hasRole
import com.centyllion.backend.isOwner
import com.centyllion.backend.withRequiredPrincipal
import com.centyllion.common.adminRole
import com.centyllion.model.Simulation
import com.centyllion.model.SimulationDescription
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

fun Route.simulation(data: Data) {
    route("simulation") {
        get {
            val caller = call.principal<JWTPrincipal>()?.let {
                data.getOrCreateUserFromPrincipal(it)
            }
            val offset = (call.parameters["offset"]?.toLongOrNull() ?: 0).coerceAtLeast(0)
            val limit = (call.parameters["limit"]?.toIntOrNull() ?: 50).coerceIn(0, 50)
            val userId = call.parameters["user"]
            val modelId = call.parameters["model"]
            context.respond(data.simulations(caller?.id, userId, modelId, offset, limit))
        }

        // post a new simulation
        post {
            withRequiredPrincipal {
                val user = data.getOrCreateUserFromPrincipal(it)
                val modelId = call.parameters["model"]
                val model = modelId?.let { data.getGrainModel(it) }
                val newSimulation = call.receive(Simulation::class)
                context.respond(
                    when {
                        model == null -> HttpStatusCode.NotFound
                        !hasReadAccess(model.info, user) && !it.hasRole(adminRole) -> HttpStatusCode.Unauthorized
                        else -> data.createSimulation(user.id, modelId, newSimulation)
                    }
                )
            }
        }

        get("selection") {
            val offset = (call.parameters["offset"]?.toLongOrNull() ?: 0).coerceAtLeast(0)
            val limit = (call.parameters["limit"]?.toIntOrNull() ?: 50).coerceIn(0, 50)
            context.respond(data.simulationsSelection(offset, limit))
        }

        get("monitor") {
            withRequiredPrincipal(adminRole) {
                context.respond(data.simulationsInfo())
            }
        }

        get("search") {
            val offset = (call.parameters["offset"]?.toLongOrNull() ?: 0).coerceAtLeast(0)
            val limit = (call.parameters["limit"]?.toIntOrNull() ?: 50).coerceIn(0, 50)
            val query = call.parameters["q"]?.decodeURLQueryComponent() ?: ""
            val tsquery = query.split(Regex("\\s+")).filter { it.isNotBlank() }.joinToString("&")
            context.respond(data.searchSimulation(tsquery, offset, limit))
        }

        route("{simulation}") {
            get {
                val user = call.principal<JWTPrincipal>()?.let {
                    data.getOrCreateUserFromPrincipal(it)
                }
                val simulationId = call.parameters["simulation"]!!
                val simulation = data.getSimulation(simulationId)
                context.respond(
                    when {
                        simulation == null -> HttpStatusCode.NotFound
                        !hasReadAccess(simulation.info, user) -> HttpStatusCode.Unauthorized
                        else -> simulation
                    }
                )
            }

            // patch an existing simulation for user
            patch {
                withRequiredPrincipal {
                    val user = data.getOrCreateUserFromPrincipal(it)
                    val simulationId = call.parameters["simulation"]!!
                    val simulation = call.receive(SimulationDescription::class)
                    context.respond(
                        when {
                            simulation.id != simulationId -> HttpStatusCode.Forbidden
                            // TODO block private model, need some writes
                            !simulation.info.readAccess -> HttpStatusCode.Forbidden
                            !isOwner(simulation.info, user) && !it.hasRole(adminRole) -> HttpStatusCode.Unauthorized
                            else -> {
                                data.saveSimulation(simulation)
                                HttpStatusCode.OK
                            }
                        }
                    )
                }
            }

            // delete an existing model for user
            delete {
                withRequiredPrincipal {
                    val user = data.getOrCreateUserFromPrincipal(it)
                    val simulationId = call.parameters["simulation"]!!
                    val simulation = data.getSimulation(simulationId)
                    context.respond(
                        when {
                            simulation == null -> HttpStatusCode.NotFound
                            !isOwner(simulation.info, user) && !it.hasRole(adminRole) -> HttpStatusCode.Unauthorized
                            else -> {
                                data.deleteSimulation(simulationId)
                                if (simulation.thumbnailId != null) {
                                    data.deleteAsset(simulation.thumbnailId)
                                }
                                HttpStatusCode.OK
                            }
                        }
                    )
                }
            }

            route("thumbnail") {
                get {
                    val simulationId = call.parameters["simulation"]!!
                    val simulation = data.getSimulation(simulationId)
                    if (simulation?.thumbnailId != null) {
                        val asset = data.getAssetContent(simulation.thumbnailId)
                        if (asset != null) {
                            context.respondBytes(asset, ContentType.Image.Any)
                        } else {
                            context.respond(HttpStatusCode.NotFound)
                        }
                    } else {
                        context.respond(HttpStatusCode.NotFound)
                    }
                }

                post {
                    withRequiredPrincipal {
                        val user = data.getOrCreateUserFromPrincipal(it)
                        val simulationId = call.parameters["simulation"]!!
                        val simulation = data.getSimulation(simulationId)

                        when {
                            simulation == null -> context.respond(HttpStatusCode.NotFound)
                            !isOwner(simulation.info, user) && !it.hasRole(adminRole) -> context.respond(HttpStatusCode.Unauthorized)
                            else -> {
                                // gets all parts
                                val multipart = call.receiveMultipart()
                                val parts = multipart.readAllParts()

                                val name = parts
                                    .filterIsInstance(PartData.FormItem::class.java)
                                    .firstOrNull { it.name == "name" }?.value

                                val content = parts
                                    .filterIsInstance(PartData.FileItem::class.java)
                                    .firstOrNull()?.let { it.streamProvider().use { it.readBytes()} }

                                parts.forEach { it.dispose() }

                                if (name != null && content != null) {
                                    // creates asset
                                    val asset = data.createAsset(name, user.id, content)
                                    // removes previous one if any
                                    simulation.thumbnailId?.let { data.deleteAsset(it) }
                                    // updates simulation with new id
                                    data.saveSimulation(simulation.copy(thumbnailId = asset.id))

                                    context.respond(asset.id)
                                } else {
                                    context.respond(HttpStatusCode.BadRequest)
                                }
                            }
                        }
                    }
                }
            }
        }

    }
}
