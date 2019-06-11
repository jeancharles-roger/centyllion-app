package com.centyllion.backend.route

import com.centyllion.backend.checkRoles
import com.centyllion.backend.data.Data
import com.centyllion.common.adminRole
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
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

        get("{id}") {
            // TODO handle the case when id is the principal user to accept detailed
            val detailed = call.parameters["detailed"]?.toBoolean() ?: false
            val id = call.parameters["simulation"]!!
            val user = data.getUser(id, detailed)
            context.respond(
                when {
                    user == null -> HttpStatusCode.NotFound
                    detailed && !checkRoles(adminRole) -> HttpStatusCode.Unauthorized
                    else -> user
                }
            )
        }

    }
}
