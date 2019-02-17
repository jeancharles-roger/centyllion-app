package com.centyllion

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.config.MapApplicationConfig
import io.ktor.features.*
import io.ktor.http.CacheControl
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.*
import io.ktor.http.withCharset
import io.ktor.response.respond
import io.ktor.routing.routing
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.util.KtorExperimentalAPI
import org.slf4j.event.Level

class ServerCommand : CliktCommand("Start the server") {

    val debug by option(help = "Activate debug parameters").flag(default = false)

    val host by option(help = "Host to listen").default("localhost")
    val port by option(help = "Port to listen").int().default(0)

    @KtorExperimentalAPI
    override fun run() {
        val effectivePort = if (port <= 0) 8080 else port

        val env = applicationEngineEnvironment {
            connector {
                host = this@ServerCommand.host
                port = effectivePort
            }
            watchPaths = if (debug) listOf("src/jvmMain/") else emptyList()

            val map = config as MapApplicationConfig
            map.put("debug", "$debug")

            module(Application::centyllion)

        }

        embeddedServer(Netty, env).start(wait = true)
    }
}

fun main(args: Array<String>): Unit = ServerCommand().main(args)

@KtorExperimentalAPI
fun Application.centyllion() {
    val debug = environment.config.property("debug").getString() == "true"

    install(Compression)
    install(DefaultHeaders)
    install(AutoHeadResponse)
    install(CachingHeaders) {
        options {
            val type = it.contentType
            when {
                type == null -> null
                type.match(ContentType.Application.Json) -> CachingOptions(CacheControl.NoCache(CacheControl.Visibility.Private))
                type.match(ContentType.Application.JavaScript) -> CachingOptions(CacheControl.MaxAge(31536000 /* one year */))
                type.match(ContentType.Image.Any) -> CachingOptions(CacheControl.MaxAge(30 * 24 * 60 * 60 /* about a month */))
                else -> CachingOptions(CacheControl.MaxAge(60 * 60 /* one hours */))
            }

        }
    }

    install(CallLogging) {
        if (debug) level = Level.TRACE else level = Level.WARN
    }

    // TODO create nice error pages
    install(StatusPages) {
        status(HttpStatusCode.NotFound) {
            context.respond(
                TextContent(
                    "${it.value} ${it.description}",
                    ContentType.Text.Plain.withCharset(Charsets.UTF_8),
                    it
                )
            )
        }
        status(HttpStatusCode.Unauthorized) {
            context.respond(
                TextContent(
                    "${it.value} ${it.description}",
                    ContentType.Text.Plain.withCharset(Charsets.UTF_8),
                    it
                )
            )
        }
    }

    routing {
        static {
            files("webroot")
            default("index.html")
        }

    }
}
