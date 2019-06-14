package com.centyllion.backend.route

import com.centyllion.backend.checkRoles
import com.centyllion.backend.data.Data
import com.centyllion.backend.withRequiredPrincipal
import com.centyllion.common.adminRole
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route

fun Route.user(data: Data) {
    route("user") {
        get {
            val detailed = call.parameters["detailed"]?.toBoolean() ?: false
            val offset = (call.parameters["offset"]?.toIntOrNull() ?: 0).coerceAtLeast(0)
            val limit = (call.parameters["limit"]?.toIntOrNull() ?: 50).coerceIn(0, 50)
            if (!detailed || checkRoles(adminRole)) {
                val users = data.getAllUsers(detailed, offset, limit)
                context.respond(users)
            } else {
                context.respond(HttpStatusCode.Unauthorized)
            }
        }

        route("{user}") {
            get {
                // TODO handle the case when id is the principal user to accept detailed
                val detailed = call.parameters["detailed"]?.toBoolean() ?: false
                val id = call.parameters["user"]!!
                val user = data.getUser(id, detailed)
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
                    withRequiredPrincipal {
                        val actor = data.getOrCreateUserFromPrincipal(it)
                        val all = call.parameters["all"]?.toBoolean() ?: false
                        val userId = call.parameters["user"]!!
                        val user = data.getUser(userId, false)
                        context.respond(when {
                            user == null -> HttpStatusCode.NotFound
                            actor.id != user.id && !checkRoles(adminRole) -> HttpStatusCode.Unauthorized
                            else -> data.subscriptionsForUser(user, all)
                        })
                    }
                }

                get("{id}") {
                    withRequiredPrincipal {
                        val actor = data.getOrCreateUserFromPrincipal(it)
                        val userId = call.parameters["user"]!!
                        val id = call.parameters["id"]!!
                        val subscription = data.getSubscription(id)
                        context.respond(
                            when {
                                subscription == null || userId != subscription.userId -> HttpStatusCode.NotFound
                                subscription.userId != actor.id && !checkRoles(adminRole) -> HttpStatusCode.Unauthorized
                                else -> subscription
                            }
                        )
                    }
                }

                // post a new model
                post {
                    withRequiredPrincipal(adminRole) {
                        val actor = data.getOrCreateUserFromPrincipal(it)
                        val userId = call.parameters["user"]!!
                        val user = data.getUser(userId, false)
                        context.respond(when {
                            user == null -> HttpStatusCode.NotFound
                            else -> data.createSubscription(user, true, 1, "creator", 0.0, "test")
                       })
                    }
                }
            }
        }

    }
}

