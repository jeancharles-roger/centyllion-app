package com.centyllion.backend.route

import com.centyllion.backend.SubscriptionManager
import com.centyllion.backend.data.Data
import com.centyllion.backend.withRequiredPrincipal
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
        get("model") {
            withRequiredPrincipal {
                val user = subscription.getOrCreateUserFromPrincipal(it)
                val models = data.grainModelsForUser(user.id)
                context.respond(models)
            }
        }

        get("subscription") {
            withRequiredPrincipal {
                val user = subscription.getOrCreateUserFromPrincipal(it)
                context.respond(data.subscriptionsForUser(user.id))
            }
        }
    }
}
