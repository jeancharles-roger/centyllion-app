package com.centyllion.backend.route

import com.centyllion.backend.ServerConfig
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route

fun Route.info(options: ServerConfig) {
    route("info") {
        get {
            context.respond("""
                {"app": "Centyllion", "debug": ${options.debug}, "dry": ${options.dry} }
            """.trimIndent()
            )
        }
    }
}
