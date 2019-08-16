package com.centyllion.backend.route

import com.centyllion.backend.SubscriptionManager
import com.centyllion.backend.data.Data
import com.centyllion.backend.withRequiredPrincipal
import com.centyllion.common.adminRole
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.delete
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route

fun Route.featured(subscription: SubscriptionManager, data: Data) {
    route("featured") {
        get {
            val offset = (call.parameters["offset"]?.toIntOrNull() ?: 0).coerceAtLeast(0)
            val limit = (call.parameters["limit"]?.toIntOrNull() ?: 50).coerceIn(0, 50)
            val allFeatured = data.getAllFeatured(offset, limit)
            context.respond(allFeatured)
        }

        // post a new featured
        post {
            withRequiredPrincipal(adminRole) {
                val simulationId = call.receive(String::class)
                context.respond(
                    when (data.getSimulation(simulationId)) {
                        null -> HttpStatusCode.NotFound
                        else -> data.createFeatured(simulationId)
                    }
                )
            }
        }

        // access a given featured
        route("{featured}") {
            get {
                val id = call.parameters["featured"]!!
                val featured = data.getFeatured(id)
                context.respond(
                    when {
                        featured == null -> HttpStatusCode.NotFound
                        else -> featured
                    }
                )
            }

            // delete an existing featured
            delete {
                withRequiredPrincipal(adminRole) {
                    val user = subscription.getOrCreateUserFromPrincipal(it)
                    val id = call.parameters["featured"]!!
                    val featured = data.getFeatured(id)
                    context.respond(
                        when {
                            featured == null -> HttpStatusCode.NotFound
                            else -> {
                                data.deleteFeatured(id)
                                HttpStatusCode.OK
                            }
                        }
                    )
                }
            }
        }
    }
}
