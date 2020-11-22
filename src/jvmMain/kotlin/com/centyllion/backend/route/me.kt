package com.centyllion.backend.route

import com.centyllion.backend.data.Data
import com.centyllion.backend.withRequiredPrincipal
import com.centyllion.model.UserOptions
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route

/** Routes for `/api/me`. */
fun Route.me(data: Data) {
    route("me") {
        // get the user's profile
        get {
            withRequiredPrincipal {
                context.respond(data.getOrCreateUserFromPrincipal(it))
            }
        }

        post {
            withRequiredPrincipal {
                val options = call.receive<UserOptions>()
                val user = data.getOrCreateUserFromPrincipal(it)
                val newUser = user.copy(details = user.details?.copy(
                    tutorialDone = options.tutorialDone
                ))
                data.saveUser(newUser)
                context.respond(HttpStatusCode.OK)
            }
        }

        get("tags") {
            withRequiredPrincipal {
                val offset = (call.parameters["offset"]?.toLongOrNull() ?: 0).coerceAtLeast(0)
                val limit = (call.parameters["limit"]?.toIntOrNull() ?: 50).coerceIn(0, 50)
                val user = data.getOrCreateUserFromPrincipal(it)
                context.respond(data.modelTags(user.id, offset, limit))
            }
        }

        // get all user's saved models
        route("model") {
            get {
                withRequiredPrincipal {
                    val offset = (call.parameters["offset"]?.toLongOrNull() ?: 0).coerceAtLeast(0)
                    val limit = (call.parameters["limit"]?.toIntOrNull() ?: 50).coerceIn(0, 50)
                    val user = data.getOrCreateUserFromPrincipal(it)
                    context.respond(data.grainModels(user.id, user.id, offset, limit))
                }
            }
        }

        // get all user's saved simulations
        route("simulation") {
            get {
                withRequiredPrincipal {
                    val offset = (call.parameters["offset"]?.toLongOrNull() ?: 0).coerceAtLeast(0)
                    val limit = (call.parameters["limit"]?.toIntOrNull() ?: 50).coerceIn(0, 50)
                    val modelId = call.parameters["model"]
                    val user = data.getOrCreateUserFromPrincipal(it)
                    context.respond(data.simulations(user.id, user.id, modelId, offset, limit))
                }
            }
        }
    }
}
