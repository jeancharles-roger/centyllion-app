package com.centyllion.backend.route

import com.centyllion.backend.ServerConfig
import com.centyllion.backend.SubscriptionManager
import com.centyllion.backend.checkRoles
import com.centyllion.backend.withRequiredPrincipal
import com.centyllion.common.SubscriptionType
import com.centyllion.common.adminRole
import com.centyllion.model.SubscriptionParameters
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route

fun Route.user(subscriptionManager: SubscriptionManager, config: ServerConfig) {
    route("user") {
        get {
            val detailed = call.parameters["detailed"]?.toBoolean() ?: false
            val offset = (call.parameters["offset"]?.toIntOrNull() ?: 0).coerceAtLeast(0)
            val limit = (call.parameters["limit"]?.toIntOrNull() ?: 50).coerceIn(0, 50)
            if (!detailed || checkRoles(adminRole)) {
                val users = subscriptionManager.getAllUsers(detailed, offset, limit)
                context.respond(users)
            } else {
                context.respond(HttpStatusCode.Unauthorized)
            }
        }

        route("{user}") {
            get {
                val detailed = call.parameters["detailed"]?.toBoolean() ?: false
                val id = call.parameters["user"]!!
                val user = subscriptionManager.getUser(id, detailed)
                context.respond(
                    when {
                        user == null -> HttpStatusCode.NotFound
                        detailed && !checkRoles(adminRole) -> HttpStatusCode.Unauthorized
                        else -> user
                    }
                )
            }

            route("subscription") {
                get {
                    withRequiredPrincipal(adminRole) {
                        val userId = call.parameters["user"]!!
                        val user = subscriptionManager.getUser(userId, false)
                        context.respond(
                            when (user) {
                                null -> HttpStatusCode.NotFound
                                else -> config.data.subscriptionsForUser(userId)
                            }
                        )
                    }
                }

                get("{id}") {
                    withRequiredPrincipal(adminRole) {
                        val userId = call.parameters["user"]!!
                        val id = call.parameters["id"]!!
                        val subscription = config.data.getSubscription(id)
                        context.respond(
                            when {
                                subscription == null || userId != subscription.userId -> HttpStatusCode.NotFound
                                else -> subscription
                            }
                        )
                    }
                }

                // post a new subscription
                post {
                    withRequiredPrincipal(adminRole) {
                        val userId = call.parameters["user"]!!
                        val user = subscriptionManager.getUser(userId, true)
                        val parameters = call.receive(SubscriptionParameters::class)
                        context.respond(
                            when {
                                user?.details == null -> HttpStatusCode.NotFound
                                parameters.subscription == SubscriptionType.Apprentice -> HttpStatusCode.BadRequest
                                else -> subscriptionManager.create(userId, parameters).let {
                                    if (it.paymentMethod == "manual") subscriptionManager.validate(it, user, true) else it
                                }
                            }
                        )
                    }
                }
            }
        }

    }
}

