package com.centyllion.backend

import com.auth0.jwk.JwkProvider
import com.auth0.jwk.JwkProviderBuilder
import com.auth0.jwt.JWTVerifier
import com.centyllion.backend.data.Data
import com.centyllion.backend.route.*
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
const val masterRealm = "master"
const val masterLogin = "automation"
const val masterClient = "admin-cli"
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
    // start server command
    ServerCommand().main(args)
}

@KtorExperimentalAPI
fun Application.centyllion(
    debug: Boolean, data: Data, subscription: SubscriptionManager, verifier: JWTVerifier? = null
) {
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
        logger = LoggerFactory.getLogger(Application::class.java)
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
                    if (debug) cause.printStackTrace()

                    // insert an event when see a problem
                    val principal = call.principal<JWTPrincipal>()
                    val user = principal?.let { data.getOrCreateUserFromPrincipal(it) }
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
            if (verifier != null) {
                verifier(verifier)
            } else {
                verifier(makeJwkProvider(), realmBase) {
                    acceptLeeway(5)
                }
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
                me(data)
                user(data)
                featured(data)
                model(data)
                simulation(data)
                asset(data)
            }
        }
    }
}

/** Checks if [info] authorizes access for [user] */
fun hasReadAccess(info: DescriptionInfo, user: User?) =
    info.readAccess || (user != null && isOwner(info, user))

fun isOwner(info: DescriptionInfo, user: User) = info.userId == user.id

suspend fun PipelineContext<Unit, ApplicationCall>.withRequiredPrincipal(
    requiredRole: String? = null, block: suspend (JWTPrincipal) -> Unit
) {
    // test if authenticated and all required roles are present
    val principal = call.principal<JWTPrincipal>()
    val granted = if (requiredRole == null) true else {
        val rolesClaim = principal?.payload?.getClaim("roles")
        val roles: List<String> = rolesClaim?.asList<String>(String::class.java) ?: emptyList()
        roles.contains(requiredRole)
    }
    if (principal != null && granted) {
        block(principal)
    } else {
        context.respond(HttpStatusCode.Unauthorized)
    }
}
