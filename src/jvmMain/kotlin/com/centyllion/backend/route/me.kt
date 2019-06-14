package com.centyllion.backend.route

import com.centyllion.backend.data.Data
import com.centyllion.backend.withRequiredPrincipal
import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
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

        // get all user's saved models
        get("model") {
            withRequiredPrincipal {
                val user = data.getOrCreateUserFromPrincipal(it)
                val models = data.grainModelsForUser(user)
                context.respond(models)
            }
        }

        get("subscription") {
            withRequiredPrincipal {
                val all = call.parameters["all"]?.toBoolean() ?: false
                val user = data.getOrCreateUserFromPrincipal(it)
                context.respond(data.subscriptionsForUser(user, all))
            }
        }
    }
}
