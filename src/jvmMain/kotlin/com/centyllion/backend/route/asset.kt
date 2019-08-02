package com.centyllion.backend.route

import com.centyllion.backend.SubscriptionManager
import com.centyllion.backend.data.Data
import com.centyllion.backend.withRequiredPrincipal
import com.centyllion.common.adminRole
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.readAllParts
import io.ktor.http.content.streamProvider
import io.ktor.request.receiveMultipart
import io.ktor.response.respond
import io.ktor.response.respondBytes
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route

fun Route.asset(subscription: SubscriptionManager, data: Data) {
    route("asset") {
        get {
            withRequiredPrincipal(adminRole) {
                val offset = (call.parameters["offset"]?.toIntOrNull() ?: 0).coerceAtLeast(0)
                val limit = (call.parameters["limit"]?.toIntOrNull() ?: 50).coerceIn(0, 50)
                val extensions = call.parameters.getAll("extension") ?: emptyList()
                context.respond(data.getAllAssets(offset, limit, extensions))
            }
        }

        get("{asset}") {
            val id = call.parameters["asset"]!!
            val asset = data.getAssetContent(id)
            if (asset != null) {
                context.respondBytes(asset, ContentType.Image.PNG)
            } else {
                context.respond(HttpStatusCode.NotFound)
            }
        }

        post {
            withRequiredPrincipal(adminRole) {
                val user = subscription.getOrCreateUserFromPrincipal(it)

                val multipart = call.receiveMultipart()
                val parts = multipart.readAllParts()

                val name = parts
                    .filterIsInstance(PartData.FormItem::class.java)
                    .firstOrNull { it.name == "name" }?.value

                val content = parts
                    .filterIsInstance(PartData.FileItem::class.java)
                    .firstOrNull()?.let { it.streamProvider().use { it.readBytes()} }

                parts.forEach { it.dispose() }

                if (name != null && content != null) {
                    val asset = data.createAsset(name, user.id, content)
                    context.respond(asset.id)
                } else {
                    context.respond(HttpStatusCode.BadRequest)
                }
            }
        }
    }
}
