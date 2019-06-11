package com.centyllion.backend.route

import com.centyllion.backend.data.Data
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.response.respondBytes
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route

fun Route.asset(data: Data) {
    route("asset") {
        get("{asset}") {
            val id = call.parameters["asset"]!!
            val asset = data.getAsset(id)
            if (asset != null) {
                context.respondBytes(asset.data, ContentType.Image.PNG)
            } else {
                context.respond(HttpStatusCode.NotFound)
            }
        }
    }
}
