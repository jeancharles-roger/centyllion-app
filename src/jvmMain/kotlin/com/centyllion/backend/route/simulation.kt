package com.centyllion.backend.route

import com.centyllion.backend.data.Data
import com.centyllion.backend.hasReadAccess
import com.centyllion.backend.isOwner
import com.centyllion.backend.withRequiredPrincipal
import com.centyllion.common.creatorRole
import com.centyllion.model.SimulationDescription
import io.ktor.application.call
import io.ktor.auth.jwt.JWTPrincipal
import io.ktor.auth.principal
import io.ktor.http.HttpStatusCode
import io.ktor.http.decodeURLQueryComponent
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*

fun Route.simulation(data: Data) {
    route("simulation") {
        get {
            val offset = (call.parameters["offset"]?.toIntOrNull() ?: 0).coerceAtLeast(0)
            val limit = (call.parameters["limit"]?.toIntOrNull() ?: 50).coerceIn(0, 50)
            val simulations = data.publicSimulations(offset, limit)
            context.respond(simulations)
        }

        get("search") {
            val offset = (call.parameters["offset"]?.toIntOrNull() ?: 0).coerceAtLeast(0)
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
                withRequiredPrincipal(creatorRole) {
                    val user = data.getOrCreateUserFromPrincipal(it)
                    val simulationId = call.parameters["simulation"]!!
                    val simulation = call.receive(SimulationDescription::class)
                    context.respond(
                        when {
                            simulation.id != simulationId -> HttpStatusCode.Forbidden
                            !isOwner(simulation.info, user) -> HttpStatusCode.Unauthorized
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
                withRequiredPrincipal(creatorRole) {
                    val user = data.getOrCreateUserFromPrincipal(it)
                    val simulationId = call.parameters["simulation"]!!
                    val simulation = data.getSimulation(simulationId)
                    context.respond(
                        when {
                            simulation == null -> HttpStatusCode.NotFound
                            !isOwner(simulation.info, user) -> HttpStatusCode.Unauthorized
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
        }

    }
}
