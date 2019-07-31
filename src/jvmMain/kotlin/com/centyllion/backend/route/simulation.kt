package com.centyllion.backend.route

import com.centyllion.backend.SubscriptionManager
import com.centyllion.backend.checkAccess
import com.centyllion.backend.data.Data
import com.centyllion.backend.hasReadAccess
import com.centyllion.backend.hasRole
import com.centyllion.backend.isOwner
import com.centyllion.backend.withRequiredPrincipal
import com.centyllion.common.apprenticeRole
import com.centyllion.common.creatorRole
import com.centyllion.model.SimulationDescription
import io.ktor.application.call
import io.ktor.auth.jwt.JWTPrincipal
import io.ktor.auth.principal
import io.ktor.http.HttpStatusCode
import io.ktor.http.decodeURLQueryComponent
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.delete
import io.ktor.routing.get
import io.ktor.routing.patch
import io.ktor.routing.route

fun Route.simulation(subscription: SubscriptionManager, data: Data) {
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
                    subscription.getOrCreateUserFromPrincipal(it)
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
                withRequiredPrincipal(apprenticeRole) {
                    val user = subscription.getOrCreateUserFromPrincipal(it)
                    val simulationId = call.parameters["simulation"]!!
                    val canPublish = it.hasRole(creatorRole)
                    val simulation = call.receive(SimulationDescription::class).checkAccess(canPublish)
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
                withRequiredPrincipal(apprenticeRole) {
                    val user = subscription.getOrCreateUserFromPrincipal(it)
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
