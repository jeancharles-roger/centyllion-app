package com.centyllion.backend

import com.auth0.jwk.JwkProvider
import com.auth0.jwk.JwkProviderBuilder
import com.auth0.jwt.JWTVerifier
import com.centyllion.common.adminRole
import com.centyllion.common.creatorRole
import com.centyllion.model.*
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
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondBytes
import io.ktor.routing.*
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
                        withRequiredPrincipal {
                            val user = data.getOrCreateUserFromPrincipal(it)
                            val models = data.grainModelsForUser(user)
                            context.respond(models)
                        }
                    }
                }

                route("user") {
                    get {

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
                        withRequiredPrincipal(adminRole) {
                            val user = data.getOrCreateUserFromPrincipal(it)
                            val newFeatured = call.receive(FeaturedDescription::class)
                            val model = data.getGrainModel(newFeatured.modelId)
                            val simulation = data.getSimulation(newFeatured.simulationId)
                            val author = data.getUser(newFeatured.authorId, false)

                            context.respond(
                                when {
                                    model == null || simulation == null || author == null -> HttpStatusCode.NotFound
                                    model.info.userId != author.id && simulation.info.userId != author.id -> HttpStatusCode.Unauthorized
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
                            withRequiredPrincipal(adminRole) {
                                val user = data.getOrCreateUserFromPrincipal(it)
                                val id = call.parameters["featured"]!!
                                val featured = data.getFeatured(id)
                                context.respond(
                                    when {
                                        featured == null -> HttpStatusCode.NotFound
                                        else -> {
                                            data.deleteFeatured(user, id)
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
                        val models = data.publicGrainModels(0, 50)
                        context.respond(models)
                    }

                    get("search") {
                        val query = call.parameters["q"]!!
                        context.respond(data.searchModel(query))
                    }

                    // post a new model
                    post {
                        withRequiredPrincipal(creatorRole) {
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
                            withRequiredPrincipal(creatorRole) {
                                val user = data.getOrCreateUserFromPrincipal(it)
                                val id = call.parameters["model"]!!
                                val model = call.receive(GrainModelDescription::class)
                                context.respond(
                                    when {
                                        model.id != id -> HttpStatusCode.Forbidden
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
                            withRequiredPrincipal(creatorRole) {
                                val user = data.getOrCreateUserFromPrincipal(it)
                                val id = call.parameters["model"]!!
                                val model = data.getGrainModel(id)
                                context.respond(
                                    when {
                                        model == null -> HttpStatusCode.NotFound
                                        !isOwner(model.info, user) -> HttpStatusCode.Unauthorized
                                        else -> {
                                            data.deleteGrainModel(user, id)
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
                                            simulations.filter {
                                                hasReadAccess(
                                                    it.info,
                                                    if (publicOnly) null else user
                                                )
                                            }
                                        }
                                    }
                                )
                            }

                            // post a new simulation for model
                            post {
                                withRequiredPrincipal(creatorRole) {
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

                    get("search") {
                        val query = call.parameters["q"]!!
                        context.respond(data.searchSimulation(query))
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
                            withRequiredPrincipal(creatorRole) {
                                val user = data.getOrCreateUserFromPrincipal(it)
                                val simulationId = call.parameters["simulation"]!!
                                val simulation = call.receive(SimulationDescription::class)
                                context.respond(
                                    when {
                                        simulation.id != simulationId -> HttpStatusCode.Forbidden
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
                            withRequiredPrincipal(creatorRole) {
                                val user = data.getOrCreateUserFromPrincipal(it)
                                val simulationId = call.parameters["simulation"]!!
                                val simulation = data.getSimulation(simulationId)
                                context.respond(
                                    when {
                                        simulation == null -> HttpStatusCode.NotFound
                                        !isOwner(simulation.info, user) -> HttpStatusCode.Unauthorized
                                        else -> {
                                            data.deleteSimulation(user, simulationId)
                                            if (simulation.thumbnailId != null) {
                                                data.deleteAsset(simulation.thumbnailId)
                                            }
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
