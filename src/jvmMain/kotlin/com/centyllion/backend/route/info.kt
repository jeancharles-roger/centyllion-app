package com.centyllion.backend.route

import com.centyllion.backend.ServerConfig
import com.centyllion.model.Info
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route

fun Route.info(options: ServerConfig) {
    route("info") {
        get {
            context.respond(Info("Centyllion", options.debug, options.dry))
        }
    }
}
