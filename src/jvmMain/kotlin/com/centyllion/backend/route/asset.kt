package com.centyllion.backend.route

import com.centyllion.backend.SubscriptionManager
import com.centyllion.backend.data.Data
import com.centyllion.backend.withRequiredPrincipal
import com.centyllion.common.adminRole
import com.centyllion.common.creatorRole
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.readAllParts
import io.ktor.http.content.streamProvider
import io.ktor.http.fromFileExtension
import io.ktor.request.receiveMultipart
import io.ktor.response.respond
import io.ktor.response.respondBytes
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import java.io.ByteArrayInputStream
import java.util.WeakHashMap
import java.util.zip.ZipInputStream

fun Route.asset(subscription: SubscriptionManager, data: Data) {

    val zipAssetCache = WeakHashMap<String, ByteArray?>()

    route("asset") {
        get {
            withRequiredPrincipal(creatorRole) {
                val offset = (call.parameters["offset"]?.toIntOrNull() ?: 0).coerceAtLeast(0)
                val limit = (call.parameters["limit"]?.toIntOrNull() ?: 50).coerceIn(0, 50)
                val extensions = call.parameters.getAll("extension") ?: emptyList()
                context.respond(data.getAllAssets(offset, limit, extensions))
            }
        }

        route("{asset}") {
            get {
                val id = call.parameters["asset"]!!
                val asset = data.getAsset(id)
                context.respond(asset ?: HttpStatusCode.NotFound)
            }
            
            route("{name}") {
                get {
                    val id = call.parameters["asset"]!!
                    val name = call.parameters["name"]!!
                    val asset = data.getAsset(id)
                    val content = data.getAssetContent(id)
                    if (asset != null && content != null && asset.name == name) {
                        val contentType = ContentType.fromFileExtension(name.substringAfterLast('.', ""))
                        context.respondBytes(content, contentType.firstOrNull() ?: ContentType.Application.OctetStream)
                    } else {
                        context.respond(HttpStatusCode.NotFound)
                    }
                }

                get("{entry...}") {
                    val id = call.parameters["asset"]!!
                    val name = call.parameters["name"]!!
                    val file = call.parameters.getAll("entry")?.joinToString("/") ?: ""
                    val asset = data.getAsset(id)
                    val content = data.getAssetContent(id)
                    val nameExt = name.substringAfterLast('.', "")
                    if (asset != null && content != null && asset.name == name && nameExt == "zip") {
                        // retrieves the entry from the content or from the cache
                        val entryContent = zipAssetCache.getOrPut("$id/$file") {
                            ZipInputStream(ByteArrayInputStream(content)).use {
                                var entry = it.nextEntry
                                while (entry != null) {
                                    if (entry.name == file) break
                                    entry = it.nextEntry
                                }
                                if (entry != null) it.readAllBytes() else null
                            }
                        }

                        if (entryContent != null) {
                            val ext = name.substringAfterLast('.', "")
                            val contentType = ContentType.fromFileExtension(ext)
                            context.respondBytes(entryContent, contentType.firstOrNull() ?: ContentType.Application.OctetStream)
                        } else {
                            context.respond(HttpStatusCode.NotFound)
                        }
                    } else {
                        context.respond(HttpStatusCode.NotFound)
                    }
                }
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
