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
import java.util.*

class Server : Common(
    name = "server",
    help = """
       Run server to answer HTTP api
    """,
) {
    val serverHost: String by option(
        "--server-host", help = "Server listening host"
    ).default("0.0.0.0")

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
                try {
                    if (!query.isNullOrBlank()) {
                        val result = database.databaseQueries.searchSimulation(query)
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
            get("/simulation/{id}") {
                val id = call.parameters["id"]
                try {
                    // search for simulation with id
                    val (simulation, model) = database.databaseQueries
                        .selectSimulation(UUID.fromString(id))
                        .executeAsOne()

                    if (simulation != null && model != null) {
                        // if found, construct the model to provide
                        call.respond(
                            ModelAndSimulation(
                                model = Json.decodeFromString<GrainModel>(model),
                                simulation = Json.decodeFromString<Simulation>(simulation)
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