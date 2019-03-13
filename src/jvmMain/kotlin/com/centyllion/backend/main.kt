package com.centyllion.backend

import com.auth0.jwk.JwkProvider
import com.auth0.jwk.JwkProviderBuilder
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.authenticate
import io.ktor.auth.jwt.JWTPrincipal
import io.ktor.auth.jwt.jwt
import io.ktor.auth.principal
import io.ktor.config.MapApplicationConfig
import io.ktor.features.*
import io.ktor.html.respondHtml
import io.ktor.http.CacheControl
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.CachingOptions
import io.ktor.http.content.TextContent
import io.ktor.http.content.files
import io.ktor.http.content.static
import io.ktor.http.withCharset
import io.ktor.network.tls.certificates.generateCertificate
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.engine.sslConnector
import io.ktor.server.netty.Netty
import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.pipeline.PipelineContext
import org.slf4j.event.Level
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.security.KeyStore
import java.util.concurrent.TimeUnit


const val authRealm = "Centyllion"
const val authBase = "https://login.centyllion.com/auth/realms/$authRealm"
const val authClient = "webclient"
const val jwkUrl = "$authBase/protocol/openid-connect/certs"

private fun makeJwkProvider(): JwkProvider = JwkProviderBuilder(URL(jwkUrl))
    .cached(10, 24, TimeUnit.HOURS)
    .rateLimited(10, 1, TimeUnit.MINUTES)
    .build()

const val certificateKeyAlias = "key"
const val certificatePassword = "changeit"
val passwordProvider = { certificatePassword.toCharArray() }
val certificatePath: Path = Paths.get("certificate.jks")

private fun retrieveKeystore(): KeyStore {
    return if (Files.exists(certificatePath)) Files.newInputStream(certificatePath).use {
        val keyStore = KeyStore.getInstance("JKS")!!
        keyStore.load(it, certificatePassword.toCharArray())
        keyStore
    } else {
        // TODO use here a proper certificate, this is only for debug
        generateCertificate(
            certificatePath.toFile(),
            keyAlias = certificateKeyAlias,
            keyPassword = certificatePassword,
            jksPassword = certificatePassword
        )
    }
}

class ServerCommand : CliktCommand("Start the server") {

    val ssl by option(help = "Uses a ssl connector").flag(default = false)

    val debug by option(help = "Activate debug parameters").flag(default = false)

    val host by option(help = "Host to listen").default("localhost")
    val port by option(help = "Port to listen").int().default(0)

    @KtorExperimentalAPI
    override fun run() {
        val effectivePort = if (port <= 0) if (ssl) 8443 else 8080 else port
        // Reads or generates a certificate for HTTPS
        val keystore = retrieveKeystore()

        val env = applicationEngineEnvironment {
            if (ssl) {
                sslConnector(
                    keystore,
                    certificateKeyAlias,
                    passwordProvider,
                    passwordProvider
                ) {
                    host = this@ServerCommand.host
                    port = effectivePort
                }
            } else {
                connector {
                    host = this@ServerCommand.host
                    port = effectivePort
                }
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

    install(Authentication) {
        jwt {
            verifier(makeJwkProvider(), authBase)
            realm = authRealm
            validate {
                if (it.payload.audience.contains(authClient)) JWTPrincipal(it.payload) else null
            }
        }
    }

    install(ContentNegotiation) {
        register(ContentType.Application.Json, JsonConverter())
    }

    val data = Data("localhost", 27017)
    routing {
        get("/") { context.respondHtml { index() } }

        static {
            files("webroot")
        }

        authenticate {
            route("/api") {
                // me route for user own data
                route("me") {
                    // get the user's profile
                    get {
                        withPrincipal {
                            context.respond(data.getOrCreateUserFromPrincipal(it))
                        }
                    }
                }
            }
        }
    }
}

suspend fun PipelineContext<Unit, ApplicationCall>.withPrincipal(
    requiredRoles: Set<String> = emptySet(),
    block: suspend (JWTPrincipal) -> Unit
) {
    // test if authenticated and all required roles are present
    val principal = call.principal<JWTPrincipal>()
    val granted = if (requiredRoles.isEmpty()) true else {
        val rolesClaim = principal?.payload?.getClaim("roles")
        val roles: List<String> = rolesClaim?.asList<String>(String::class.java) ?: emptyList()
        requiredRoles.fold(true) { a, c -> a && roles.contains(c) }
    }
    if (principal != null && granted) {
        block(principal)
    } else {
        context.respond(HttpStatusCode.Unauthorized)
    }
}