package com.centyllion.backend.route

import com.centyllion.backend.data.Data
import com.centyllion.backend.withRequiredPrincipal
import com.centyllion.common.adminRole
import com.centyllion.model.FeaturedDescription
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*

fun Route.featured(data: Data) {
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
                val user = data.getOrCreateUserFromPrincipal(it)
                val newFeatured = call.receive(FeaturedDescription::class)
                val model = data.getGrainModel(newFeatured.modelId)
                val simulation = data.getSimulation(newFeatured.simulationId)
                val author = data.getUser(newFeatured.authorId, false)

                context.respond(
                    when {
                        model == null || simulation == null || author == null -> HttpStatusCode.NotFound
                        model.info.userId != author.id && simulation.info.userId != author.id -> HttpStatusCode.Unauthorized
                        else -> data.createFeatured(user, model, simulation, author)
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
                    val user = data.getOrCreateUserFromPrincipal(it)
                    val id = call.parameters["featured"]!!
                    val featured = data.getFeatured(id)
                    context.respond(
                        when {
                            featured == null -> HttpStatusCode.NotFound
                            else -> {
                                data.deleteFeatured(user, id)
                                HttpStatusCode.OK
                            }
                        }
                    )
                }
            }
        }
    }
}
