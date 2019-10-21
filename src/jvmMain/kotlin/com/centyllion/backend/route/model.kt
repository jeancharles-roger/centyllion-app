package com.centyllion.backend.route

import com.centyllion.backend.data.Data
import com.centyllion.backend.hasReadAccess
import com.centyllion.backend.isOwner
import com.centyllion.backend.withRequiredPrincipal
import com.centyllion.model.GrainModel
import com.centyllion.model.GrainModelDescription
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
import io.ktor.routing.post
import io.ktor.routing.route

fun Route.model(data: Data) {
    route("model") {
        get {
            val caller = call.principal<JWTPrincipal>()?.let {
                data.getOrCreateUserFromPrincipal(it)
            }
            val offset = (call.parameters["offset"]?.toIntOrNull() ?: 0).coerceAtLeast(0)
            val limit = (call.parameters["limit"]?.toIntOrNull() ?: 50).coerceIn(0, 50)
            val userId = call.parameters["user"]
            context.respond(data.grainModels(caller?.id, userId, offset, limit))
        }

        get("tags") {
            val offset = (call.parameters["offset"]?.toIntOrNull() ?: 0).coerceAtLeast(0)
            val limit = (call.parameters["limit"]?.toIntOrNull() ?: 50).coerceIn(0, 50)
            context.respond(data.modelTags(null, offset, limit))
        }

        get("search") {
            val offset = (call.parameters["offset"]?.toIntOrNull() ?: 0).coerceAtLeast(0)
            val limit = (call.parameters["limit"]?.toIntOrNull() ?: 50).coerceIn(0, 50)
            val query = call.parameters["q"]?.decodeURLQueryComponent() ?: ""
            val tags = call.parameters["tags"]?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
            val tsquery = query.split(Regex("\\s+")).filter { it.isNotBlank() }.joinToString("&")
            context.respond(data.searchModel(tsquery, tags, offset, limit))
        }

        // post a new model
        post {
            withRequiredPrincipal {
                val user = data.getOrCreateUserFromPrincipal(it)
                val newModel = call.receive(GrainModel::class)
                val newDescription = data.createGrainModel(user.id, newModel)
                context.respond(newDescription)
            }
        }

        // access a given model
        route("{model}") {
            // model get with user
            get {
                val user = call.principal<JWTPrincipal>()?.let {
                    data.getOrCreateUserFromPrincipal(it)
                }
                val id = call.parameters["model"]!!
                val model = data.getGrainModel(id)
                context.respond(
                    when {
                        model == null -> HttpStatusCode.NotFound
                        !hasReadAccess(model.info, user) -> HttpStatusCode.Unauthorized
                        else -> model
                    }
                )
            }

            // patch an existing model
            patch {
                withRequiredPrincipal {
                    val user = data.getOrCreateUserFromPrincipal(it)
                    val id = call.parameters["model"]!!
                    val model = call.receive(GrainModelDescription::class)
                    context.respond(
                        when {
                            model.id != id -> HttpStatusCode.Forbidden
                            // TODO block private model, need some writes
                            !model.info.readAccess -> HttpStatusCode.Forbidden
                            !isOwner(model.info, user) -> HttpStatusCode.Unauthorized
                            else -> {
                                data.saveGrainModel(model)
                                HttpStatusCode.OK
                            }
                        }
                    )
                }
            }

            // delete an existing model
            delete {
                withRequiredPrincipal {
                    val user = data.getOrCreateUserFromPrincipal(it)
                    val id = call.parameters["model"]!!
                    val model = data.getGrainModel(id)
                    context.respond(
                        when {
                            model == null -> HttpStatusCode.NotFound
                            !isOwner(model.info, user) -> HttpStatusCode.Unauthorized
                            else -> {
                                data.deleteGrainModel(id)
                                HttpStatusCode.OK
                            }
                        }
                    )
                }
            }
        }
    }
}
