package com.centyllion.backend.route

import com.centyllion.backend.SubscriptionManager
import com.centyllion.backend.data.Data
import com.centyllion.backend.withRequiredPrincipal
import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route

/** Routes for `/api/me`. */
fun Route.me(subscription: SubscriptionManager, data: Data) {
    route("me") {
        // get the user's profile
        get {
            withRequiredPrincipal {
                context.respond(subscription.getOrCreateUserFromPrincipal(it))
            }
        }

        // get all user's saved models
        route("model") {
            get {
                withRequiredPrincipal {
                    val offset = (call.parameters["offset"]?.toIntOrNull() ?: 0).coerceAtLeast(0)
                    val limit = (call.parameters["limit"]?.toIntOrNull() ?: 50).coerceIn(0, 50)
                    val user = subscription.getOrCreateUserFromPrincipal(it)
                    context.respond(data.grainModelsForUser(user.id, offset, limit))
                }
            }

            get("all") {
                withRequiredPrincipal {
                    val user = subscription.getOrCreateUserFromPrincipal(it)
                    context.respond(data.allGrainModelsForUser(user.id))
                }
            }
        }

        // get all user's saved simulations
        route("simulation") {
            get {
                withRequiredPrincipal {
                    val offset = (call.parameters["offset"]?.toIntOrNull() ?: 0).coerceAtLeast(0)
                    val limit = (call.parameters["limit"]?.toIntOrNull() ?: 50).coerceIn(0, 50)
                    val user = subscription.getOrCreateUserFromPrincipal(it)
                    context.respond(data.simulationsForUser(user.id, offset, limit))
                }
            }

            get("all") {
                withRequiredPrincipal {
                    val user = subscription.getOrCreateUserFromPrincipal(it)
                    context.respond(data.allSimulationsForUser(user.id))
                }
            }
        }

        get("subscription") {
            withRequiredPrincipal {
                val user = subscription.getOrCreateUserFromPrincipal(it)
                context.respond(data.subscriptionsForUser(user.id))
            }
        }

        get("asset") {
            withRequiredPrincipal {
                val user = subscription.getOrCreateUserFromPrincipal(it)
                val models = data.grainModelsForUser(user.id)
                context.respond(models)
            }
        }
    }
}
