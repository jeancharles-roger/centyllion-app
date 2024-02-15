package com.centyllion.db

import com.centyllion.model.GrainModel
import com.centyllion.model.ModelAndSimulation
import com.centyllion.model.Simulation
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import java.io.ByteArrayInputStream
import java.util.*
import java.util.zip.ZipInputStream

class Server : Common(
    name = "server",
    help = """
       Run server to answer HTTP api
    """,
) {
    val serverHost: String by option(
        "--server-host", help = "Server listening host"
    ).default("127.0.0.1")

    val serverPort: Int by option(
        "--server-port", help = "Server listening port"
    )
        .convert { it.toInt() }
        .default(4545)

    override fun run() {
        embeddedServer(
            factory = CIO,
            host = serverHost, port = serverPort,
            module = { configure() }
        ).start(wait = true)
    }

    private fun Application.configure() {
        val database = database()
        install(ContentNegotiation) { json() }
        install(CORS) {
            anyHost()
            allowHeader(HttpHeaders.ContentType)
            allowHeader(HttpHeaders.Authorization)
        }

        routing {
            get("/search/{query}") {
                val query = call.parameters["query"]
                val limit = minOf(50, call.request.queryParameters ["limit"]?.toInt() ?: 10)
                try {
                    if (!query.isNullOrBlank()) {
                        val result = database.databaseQueries.searchSimulation(query, limit.toLong())
                            .executeAsList()
                            .map { it.toString() }
                        call.respond(result)
                    } else {
                        // no query provided
                        call.respond(HttpStatusCode.BadRequest)
                    }
                } catch (e: Throwable) {
                    // something wrong appended
                    call.respond(HttpStatusCode.BadRequest, e.localizedMessage)
                }
            }
            get("/asset/{id}") {
                val id = call.parameters["id"]
                try {
                    // search for asset with id
                    val (_, _, content) = database.databaseQueries
                        .selectAsset(UUID.fromString(id))
                        .executeAsOne()

                    if (content != null) {
                        // if found, construct the model to provide
                        call.respond(content)
                    } else {
                        // simulation not found
                        call.respond(HttpStatusCode.NotFound)
                    }
                } catch (e: Throwable) {
                    // something wrong appended
                    call.respond(HttpStatusCode.BadRequest, e.localizedMessage)
                }
            }
            get("/asset/{id}/{name}") {
                val id = call.parameters["id"]
                val name = call.parameters["name"]
                try {
                    // search for asset with id
                    val (actualName, _, content) = database.databaseQueries
                        .selectAsset(UUID.fromString(id))
                        .executeAsOne()

                    if (content != null && actualName == name) {
                        // if found, construct the model to provide
                        call.respond(content)
                    } else {
                        // simulation not found
                        call.respond(HttpStatusCode.NotFound)
                    }
                } catch (e: Throwable) {
                    // something wrong appended
                    call.respond(HttpStatusCode.BadRequest, e.localizedMessage)
                }
            }
            get("/asset/{id}/{name}/{path...}") {
                val id = call.parameters["id"]!!
                val name = call.parameters["name"]!!
                val path = call.parameters.getAll("path")?.joinToString("/") ?: ""
                try {
                    // search for asset with id
                    val (actualName, entries, content) = database.databaseQueries
                        .selectAsset(UUID.fromString(id))
                        .executeAsOne()
                    val nameExt = name.substringAfterLast('.', "")

                    if (content != null && name == actualName && nameExt == "zip") {
                        // retrieves the entry from the content or from the cache
                        val entryContent = ZipInputStream(ByteArrayInputStream(content)).use {
                            var entry = it.nextEntry
                            while (entry != null) {
                                if (entry.name == path) break
                                entry = it.nextEntry
                            }
                            if (entry != null) it.readAllBytes() else null
                        }


                        if (entryContent != null) {
                            val ext = name.substringAfterLast('.', "")
                            val contentType = ContentType.fromFileExtension(ext)
                            context.respondBytes(
                                entryContent,
                                contentType.firstOrNull() ?: ContentType.Application.OctetStream
                            )
                        } else {
                            context.respond(HttpStatusCode.NotFound)
                        }
                    } else {
                        context.respond(HttpStatusCode.NotFound)
                    }
                } catch (e: Throwable) {
                    // something wrong appended
                    call.respond(HttpStatusCode.BadRequest, e.localizedMessage)
                }
            }
            get("/simulation/{id}") {
                val id = call.parameters["id"]
                try {
                    // search for simulation with id
                    val (simulation, model, thumbnail) = database.databaseQueries
                        .selectSimulation(UUID.fromString(id))
                        .executeAsOne()

                    if (simulation != null && model != null && thumbnail != null) {
                        // if found, construct the model to provide
                        call.respond(
                            ModelAndSimulation(
                                model = Json.decodeFromString<GrainModel>(model),
                                simulation = Json.decodeFromString<Simulation>(simulation),
                                thumbnail = thumbnail.toString()
                            )
                        )
                    } else {
                        // simulation not found
                        call.respond(HttpStatusCode.NotFound)
                    }
                } catch (e: Throwable) {
                    // something wrong appended
                    call.respond(HttpStatusCode.BadRequest, e.localizedMessage)
                }
            }
        }
    }
}