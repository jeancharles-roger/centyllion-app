package com.centyllion.backend

import com.auth0.jwk.JwkProvider
import com.auth0.jwk.JwkProviderBuilder
import com.centyllion.common.adminRole
import com.centyllion.common.modelRole
import com.centyllion.model.*
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
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondBytes
import io.ktor.routing.*
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

fun main(args: Array<String>) {
    // do some checking first
    checkVersionsAndMigrations()

    // start server command
    ServerCommand().main(args)
}

@KtorExperimentalAPI
fun Application.centyllion() {
    val debug = environment.config.property("debug").getString() == "true"

    val data = Data("localhost", 27017)

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
        exception<Throwable> { cause ->
            // insert an event when see a problem
            val principal = call.principal<JWTPrincipal>()
            val user = principal?.let { data.getOrCreateUserFromPrincipal(it) }
            data.insertEvent(Action.Error, user, "",
                cause.javaClass.simpleName,
                cause.message.toString(),
                cause.stackTrace.joinToString("\n") { it.toString() }
            )

            call.respond(HttpStatusCode.InternalServerError)
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

    routing {
        get("/") { context.respondHtml { index() } }

        // Static files
        static { files("webroot") }

        authenticate(optional = true) {
            route("/api") {
                // me route for user own data
                route("me") {
                    // get the user's profile
                    get {
                        withRequiredPrincipal {
                            context.respond(data.getOrCreateUserFromPrincipal(it))
                        }
                    }

                    // get all user's saved models
                    get("model") {
                        withRequiredPrincipal(setOf(modelRole)) {
                            val user = data.getOrCreateUserFromPrincipal(it)
                            val models = data.grainModelsForUser(user)
                            context.respond(models)
                        }
                    }
                }

                // featured
                route("featured") {
                    get {
                        val allFeatured = data.getAllFeatured()
                        context.respond(allFeatured)
                    }

                    // post a new featured
                    post {
                        withRequiredPrincipal(setOf(adminRole)) {
                            val user = data.getOrCreateUserFromPrincipal(it)
                            val newFeatured = call.receive(FeaturedDescription::class)
                            val model = data.getGrainModel(newFeatured.modelId)
                            val simulation = data.getSimulation(newFeatured.simulationId)
                            val author = data.getUser(newFeatured.authorId)

                            context.respond(
                                when {
                                    model == null || simulation == null || author == null -> HttpStatusCode.NotFound
                                    model.info.userId != author._id && simulation.info.userId != author._id -> HttpStatusCode.Unauthorized
                                    else -> data.createFeatured(user, model, simulation, author)
                                }
                            )
                        }
                    }

                    // access a given featured
                    route("{featured}") {
                        get {
                            val id = call.parameters["featured"]!!
                            val featured = data.getFeatured(id)
                            context.respond(
                                when {
                                    featured == null -> HttpStatusCode.NotFound
                                    else -> featured
                                }
                            )
                        }

                        // delete an existing featured
                        delete {
                            withRequiredPrincipal(setOf(adminRole)) {
                                val user = data.getOrCreateUserFromPrincipal(it)
                                val id = call.parameters["featured"]!!
                                val featured = data.getFeatured(id)
                                context.respond(
                                    when {
                                        featured == null -> HttpStatusCode.NotFound
                                        else -> {
                                            data.deleteFeatured(user, featured)
                                            HttpStatusCode.OK
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                // user's model access
                route("model") {
                    get {
                        val models = data.publicGrainModels(50)
                        context.respond(models)
                    }

                    // post a new model
                    post {
                        withRequiredPrincipal(setOf(modelRole)) {
                            val user = data.getOrCreateUserFromPrincipal(it)
                            val newModel = call.receive(GrainModel::class)
                            val newDescription = data.createGrainModel(user, newModel)
                            context.respond(newDescription)
                        }
                    }

                    // access a given model
                    route("{model}") {
                        // model get with user
                        get {
                            val user = call.principal<JWTPrincipal>()?.let {
                                data.getOrCreateUserFromPrincipal(it)
                            }
                            val id = call.parameters["model"]!!
                            val model = data.getGrainModel(id)
                            context.respond(
                                when {
                                    model == null -> HttpStatusCode.NotFound
                                    !hasReadAccess(model.info, user) -> HttpStatusCode.Unauthorized
                                    else -> model
                                }
                            )
                        }

                        // patch an existing model
                        patch {
                            withRequiredPrincipal(setOf(modelRole)) {
                                val user = data.getOrCreateUserFromPrincipal(it)
                                val id = call.parameters["model"]!!
                                val model = call.receive(GrainModelDescription::class)
                                context.respond(
                                    when {
                                        model._id != id -> HttpStatusCode.Forbidden
                                        !isOwner(model.info, user) -> HttpStatusCode.Unauthorized
                                        else -> {
                                            data.saveGrainModel(user, model)
                                            HttpStatusCode.OK
                                        }
                                    }
                                )
                            }
                        }

                        // delete an existing model
                        delete {
                            withRequiredPrincipal(setOf(modelRole)) {
                                val user = data.getOrCreateUserFromPrincipal(it)
                                val id = call.parameters["model"]!!
                                val model = data.getGrainModel(id)
                                context.respond(
                                    when {
                                        model == null -> HttpStatusCode.NotFound
                                        !isOwner(model.info, user) -> HttpStatusCode.Unauthorized
                                        else -> {
                                            data.deleteGrainModel(user, model)
                                            HttpStatusCode.OK
                                        }
                                    }
                                )
                            }
                        }

                        route("simulation") {
                            // model's simulations
                            get {
                                val publicOnly = call.request.queryParameters["public"] != null
                                val user = call.principal<JWTPrincipal>()?.let {
                                    data.getOrCreateUserFromPrincipal(it)
                                }
                                val modelId = call.parameters["model"]!!
                                val model = data.getGrainModel(modelId)
                                context.respond(
                                    when {
                                        model == null -> HttpStatusCode.NotFound
                                        !hasReadAccess(model.info, user) -> HttpStatusCode.Unauthorized
                                        else -> {
                                            val simulations = data.getSimulationForModel(modelId)
                                            simulations.filter { hasReadAccess(it.info, if (publicOnly) null else user) }
                                        }
                                    }
                                )
                            }

                            // post a new simulation for model
                            post {
                                withRequiredPrincipal(setOf(modelRole)) {
                                    val user = data.getOrCreateUserFromPrincipal(it)
                                    val modelId = call.parameters["model"]!!
                                    val model = data.getGrainModel(modelId)
                                    val newSimulation = call.receive(Simulation::class)
                                    context.respond(
                                        when {
                                            model == null -> HttpStatusCode.NotFound
                                            !isOwner(model.info, user) -> HttpStatusCode.Unauthorized
                                            else -> data.createSimulation(user, modelId, newSimulation)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // simulations
                route("simulation") {
                    get {
                        // TODO what to do here ?
                        context.respond(HttpStatusCode.NotFound)
                    }

                    route("{simulation}") {
                        get {
                            val user = call.principal<JWTPrincipal>()?.let {
                                data.getOrCreateUserFromPrincipal(it)
                            }
                            val simulationId = call.parameters["simulation"]!!
                            val simulation = data.getSimulation(simulationId)
                            context.respond(
                                when {
                                    simulation == null -> HttpStatusCode.NotFound
                                    !hasReadAccess(simulation.info, user) -> HttpStatusCode.Unauthorized
                                    else -> simulation
                                }
                            )
                        }

                        // patch an existing simulation for user
                        patch {
                            withRequiredPrincipal(setOf(modelRole)) {
                                val user = data.getOrCreateUserFromPrincipal(it)
                                val simulationId = call.parameters["simulation"]!!
                                val simulation = call.receive(SimulationDescription::class)
                                context.respond(
                                    when {
                                        simulation._id != simulationId -> HttpStatusCode.Forbidden
                                        !isOwner(simulation.info, user) -> HttpStatusCode.Unauthorized
                                        else -> {
                                            data.saveSimulation(user, simulation)
                                            HttpStatusCode.OK
                                        }
                                    }
                                )
                            }
                        }

                        // delete an existing model for user
                        delete {
                            withRequiredPrincipal(setOf(modelRole)) {
                                val user = data.getOrCreateUserFromPrincipal(it)
                                val simulationId = call.parameters["simulation"]!!
                                val simulation = data.getSimulation(simulationId)
                                context.respond(
                                    when {
                                        simulation == null -> HttpStatusCode.NotFound
                                        !isOwner(simulation.info, user) -> HttpStatusCode.Unauthorized
                                        else -> {
                                            data.deleteSimulation(user, simulation)
                                            HttpStatusCode.OK
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                // binary assets
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

                // events
                route("event") {
                    get {
                        withRequiredPrincipal(setOf(adminRole)) {
                            val events = data.getEvents()
                            context.respond(events)
                        }
                    }
                }
            }
        }
    }
}

/** Checks if [info] authorizes access for [user] to [Access.Read]*/
fun hasReadAccess(info: DescriptionInfo, user: User?) =
    info.access.contains(Access.Read) || (user != null && isOwner(info, user))

fun isOwner(info: DescriptionInfo, user: User) = info.userId == user._id

suspend fun PipelineContext<Unit, ApplicationCall>.withRequiredPrincipal(
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
