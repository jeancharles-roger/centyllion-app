package com.centyllion.backend

import com.auth0.jwk.JwkProvider
import com.auth0.jwk.JwkProviderBuilder
import com.centyllion.backend.route.asset
import com.centyllion.backend.route.featured
import com.centyllion.backend.route.me
import com.centyllion.backend.route.model
import com.centyllion.backend.route.simulation
import com.centyllion.backend.route.user
import com.centyllion.model.DescriptionInfo
import com.centyllion.model.User
import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.authenticate
import io.ktor.auth.jwt.JWTPrincipal
import io.ktor.auth.jwt.jwt
import io.ktor.auth.principal
import io.ktor.features.AutoHeadResponse
import io.ktor.features.CachingHeaders
import io.ktor.features.CallLogging
import io.ktor.features.Compression
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.features.StatusPages
import io.ktor.html.respondHtml
import io.ktor.http.CacheControl
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.CachingOptions
import io.ktor.http.content.TextContent
import io.ktor.http.content.files
import io.ktor.http.content.static
import io.ktor.http.withCharset
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.pipeline.PipelineContext
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import java.net.URL
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.TimeUnit

const val authRealm = "Centyllion"
const val authBase = "https://login.centyllion.com/auth"
const val realmBase = "$authBase/realms/$authRealm"
const val authClient = "webclient"
const val jwkUrl = "$realmBase/protocol/openid-connect/certs"

private fun makeJwkProvider(): JwkProvider = JwkProviderBuilder(URL(jwkUrl))
    .cached(10, 24, TimeUnit.HOURS)
    .rateLimited(10, 1, TimeUnit.MINUTES)
    .build()

const val certificateKeyAlias = "key"
const val certificatePassword = "changeit"
val passwordProvider = { certificatePassword.toCharArray() }
val certificatePath: Path = Paths.get("certificate.jks")

fun main(args: Array<String>) {
    val log = LoggerFactory.getLogger(Application::class.java)
    // start server command
    ServerCommand().main(args)
}

@KtorExperimentalAPI
fun Application.centyllion(config: ServerConfig) {
    val mainLogger = LoggerFactory.getLogger(Application::class.java)
    mainLogger.info("Starting centyllion app")

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
        if (config.debug) level = Level.TRACE else level = Level.WARN
        logger = mainLogger
    }

    // TODO create nice error pages
    install(StatusPages) {
        exception<Throwable> { cause ->
            when {
                cause is IllegalArgumentException &&
                        cause.message.toString().contains("Invalid UUID string") -> {
                    call.respond(HttpStatusCode.NotFound)
                }
                else -> {
                    if (config.debug) cause.printStackTrace()

                    // insert an event when see a problem
                    val principal = call.principal<JWTPrincipal>()
                    val user = principal?.let { config.data.getOrCreateUserFromPrincipal(it) }
                    val argument = "${cause.message} ${cause.stackTrace.map { it.toString() }.joinToString { "\n" }}"
                    // TODO responds error
                    call.respond(HttpStatusCode.InternalServerError)
                }
            }
        }

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
            when (val verifier = config.verifier) {
                null -> verifier(makeJwkProvider(), realmBase) {
                    acceptLeeway(5)
                }
                else -> verifier(verifier)
            }
            realm = authRealm
            validate { if (it.payload.audience?.contains(authClient) == true) JWTPrincipal(it.payload) else null }
        }
    }

    install(ContentNegotiation) {
        register(ContentType.Application.Json, JsonConverter())
    }

    routing {
        get("/") { context.respondHtml { index() } }

        // Static files
        static { files("webroot") }

        authenticate(optional = true) {
            route("/api") {
                me(config.data)
                user(config.data, config.authorization)
                featured(config.data)
                model(config.data)
                simulation(config.data)
                asset(config.data)
            }
        }
    }
}

/** Checks if [info] authorizes access for [user] */
fun hasReadAccess(info: DescriptionInfo, user: User?) =
    info.readAccess || (user != null && isOwner(info, user))

fun isOwner(info: DescriptionInfo, user: User) = info.userId == user.id

fun PipelineContext<Unit, ApplicationCall>.checkRoles(requiredRole: String? = null) =
    if (requiredRole == null) true else {
        val principal = call.principal<JWTPrincipal>()
        val rolesClaim = principal?.payload?.getClaim("roles")
        val roles: List<String> = rolesClaim?.asList<String>(String::class.java) ?: emptyList()
        roles.contains(requiredRole)
    }

suspend fun PipelineContext<Unit, ApplicationCall>.withRequiredPrincipal(
    requiredRole: String? = null, block: suspend (JWTPrincipal) -> Unit
) = call.principal<JWTPrincipal>().let {
    // test if authenticated and all required roles are present
    if (it != null && checkRoles(requiredRole)) block(it) else context.respond(HttpStatusCode.Unauthorized)
}
