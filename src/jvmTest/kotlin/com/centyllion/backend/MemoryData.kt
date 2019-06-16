package com.centyllion.backend

import com.centyllion.backend.data.Data
import com.centyllion.common.SubscriptionType
import com.centyllion.model.*
import io.ktor.auth.jwt.JWTPrincipal
import org.joda.time.DateTime
import org.joda.time.DateTimeUtils
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max

fun newId() = UUID.randomUUID().toString()

val rfc1123Format = SimpleDateFormat("EEE, dd MMM yyyyy HH:mm:ss z", Locale.US)

fun createUser(principal: JWTPrincipal, keycloakId: String): User {
    val claims = principal.payload.claims
    val name = claims["name"]?.asString() ?: ""
    val username = claims["preferred_username"]?.asString() ?: ""
    val email = claims["email"]?.asString() ?: ""
    return User(newId(), name, username, UserDetails(keycloakId, email, null, SubscriptionType.Free))
}

fun createGrainModelDescription(user: User, sent: GrainModel) = rfc1123Format.format(Date()).let {
    GrainModelDescription(newId(), DescriptionInfo(user.id, it, it, false, false), sent)
}

fun createSubscription(
    user: User, sandbox: Boolean, duration: Int, subscription: SubscriptionType, amount: Double, paymentMethod: String
) = DateTime().let {
    Subscription(
        newId(), user.id, sandbox, false, false,
        it.millis, null, it.plusDays(duration).millis, null,
        subscription, duration, amount, paymentMethod
    )
}

fun createFeaturedDescription(
    asset: Asset, model: GrainModelDescription, simulation: SimulationDescription, author: User
) = FeaturedDescription(
    newId(), rfc1123Format.format(Date()), asset.id,
    model.id, simulation.id, author.id,
    listOf(simulation.simulation.name, model.model.name).filter { it.isNotEmpty() }.joinToString(" / "),
    listOf(simulation.simulation.description, model.model.description).filter { it.isNotEmpty() }.joinToString("\n"),
    author.name
)

/** Memory implementation for Data. Only used for tests, not optimal at all */
class MemoryData(
    val users: LinkedHashMap<String, User> = linkedMapOf(),
    val grainModels: LinkedHashMap<String, GrainModelDescription> = linkedMapOf(),
    val simulations: LinkedHashMap<String, SimulationDescription> = linkedMapOf(),
    val featured: LinkedHashMap<String, FeaturedDescription> = linkedMapOf(),
    val subscriptions: LinkedHashMap<String, Subscription> = linkedMapOf(),
    val assets: LinkedHashMap<String, Asset> = linkedMapOf()
) : Data {

    override fun getAllUsers(detailed: Boolean, offset: Int, limit: Int): ResultPage<User> =
        users.values.toList().map { if (detailed) it else it.copy(details = null) }.limit(offset, limit)

    override fun getOrCreateUserFromPrincipal(principal: JWTPrincipal) =
        users.values.find { it.details?.keycloakId == principal.payload.subject }.let {
            if (it == null) {
                val user = createUser(principal, principal.payload.subject)
                users[user.id] = user
                user
            } else {
                it
            }
        }

    override fun getUser(id: String, detailed: Boolean): User? = users[id]?.let {
        if (detailed) it else it.copy(details = null)
    }

    override fun saveUser(user: User) {
        users[user.id] = user
    }

    override fun publicGrainModels(offset: Int, limit: Int) =
        grainModels.values.toList().limit(offset, limit)

    override fun grainModelsForUser(user: User): List<GrainModelDescription> = grainModels.values.filter {
        it.info.userId == user.id
    }

    override fun getGrainModel(id: String) = grainModels[id]

    override fun createGrainModel(user: User, sent: GrainModel): GrainModelDescription {
        val model = createGrainModelDescription(user, sent)
        grainModels[model.id] = model
        return model
    }

    override fun saveGrainModel(user: User, model: GrainModelDescription) {
        grainModels[model.id] = model
    }

    override fun deleteGrainModel(user: User, modelId: String) {
        getSimulationForModel(modelId).forEach { deleteSimulation(user, it.id) }
        grainModels.remove(modelId)
    }

    override fun publicSimulations(offset: Int, limit: Int) =
        ResultPage(
            simulations.values.toList().drop(offset).dropLast(max(0, simulations.size - limit)),
            offset, simulations.size
        )


    override fun getSimulationForModel(modelId: String): List<SimulationDescription> = simulations.values.filter {
        it.modelId == modelId
    }

    override fun getSimulation(id: String) = simulations[id]

    override fun createSimulation(user: User, modelId: String, sent: Simulation): SimulationDescription {
        val result = createSimulation(user, modelId, sent)
        simulations[result.id] = result
        return result
    }

    override fun saveSimulation(user: User, simulation: SimulationDescription) {
        simulations[simulation.id] = simulation
    }

    override fun deleteSimulation(user: User, simulationId: String) {
        simulations.remove(simulationId)
    }

    override fun getAllFeatured(offset: Int, limit: Int) =
        featured.values.toList().limit(offset, limit)

    override fun getFeatured(id: String) = featured[id]

    override fun createFeatured(
        user: User, model: GrainModelDescription, simulation: SimulationDescription, author: User
    ): FeaturedDescription {
        val asset = createAsset("simulation.png", createThumbnail(model.model, simulation.simulation))
        val new = createFeaturedDescription(asset, model, simulation, author)
        featured[new.id] = new
        return new
    }

    override fun deleteFeatured(user: User, featuredId: String) {
        // delete the featured
        featured.remove(featuredId)
    }

    override fun searchSimulation(query: String, offset: Int, limit: Int) = simulations.values
        .filter { it.simulation.name.contains(query) || it.simulation.description.contains(query) }
        .limit(offset, limit)

    override fun searchModel(query: String, offset: Int, limit: Int) = grainModels.values
        .filter { it.model.name.contains(query) || it.model.description.contains(query) }
        .limit(offset, limit)

    override fun subscriptionsForUser(user: User, all: Boolean) = DateTimeUtils.currentTimeMillis().let { now ->
        subscriptions.values.filter {
            it.userId == user.id && (all || it.active(now) )
        }
    }

    override fun getSubscription(id: String): Subscription? = subscriptions[id]

    override fun createSubscription(
        user: User, sandbox: Boolean, duration: Int, type: SubscriptionType, amount: Double, paymentMethod: String
    ): Subscription {
        val new = createSubscription(user, sandbox, duration, type, amount, paymentMethod)
        subscriptions[new.id] = new
        return new
    }

    override fun saveSubscription(user: User, subscription: Subscription) {
        subscriptions[subscription.id] = subscription
    }

    override fun deleteSubscription(user: User, subscriptionId: String) {
        subscriptions.remove(subscriptionId)
    }

    override fun getAsset(id: String) = assets[id]

    override fun createAsset(name: String, data: ByteArray): Asset {
        val result = Asset(newId(), name, data)
        assets[newId()] = result
        return result
    }

    override fun deleteAsset(id: String) {
        assets.remove(id)
    }

    private fun <T> List<T>.limit(offset: Int, limit: Int) =
        ResultPage<T>(this.dropLast(max(0, this.size - limit)), offset, this.size)
}
