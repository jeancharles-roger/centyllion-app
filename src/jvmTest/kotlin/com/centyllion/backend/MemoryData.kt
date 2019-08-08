package com.centyllion.backend

import com.centyllion.backend.data.Data
import com.centyllion.common.SubscriptionType
import com.centyllion.model.Asset
import com.centyllion.model.DescriptionInfo
import com.centyllion.model.FeaturedDescription
import com.centyllion.model.GrainModel
import com.centyllion.model.GrainModelDescription
import com.centyllion.model.ResultPage
import com.centyllion.model.Simulation
import com.centyllion.model.SimulationDescription
import com.centyllion.model.Subscription
import com.centyllion.model.SubscriptionParameters
import com.centyllion.model.User
import com.centyllion.model.UserDetails
import io.ktor.auth.jwt.JWTPrincipal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.LinkedHashMap
import java.util.Locale
import java.util.UUID
import kotlin.math.max

fun newId() = UUID.randomUUID().toString()

val rfc1123Format = SimpleDateFormat("EEE, dd MMM yyyyy HH:mm:ss z", Locale.US)

fun createUser(principal: JWTPrincipal, keycloakId: String): User {
    val claims = principal.payload.claims
    val name = claims["name"]?.asString() ?: ""
    val username = claims["preferred_username"]?.asString() ?: ""
    val email = claims["email"]?.asString() ?: ""
    return User(newId(), name, username, UserDetails(keycloakId, email, null, SubscriptionType.Apprentice, null))
}

fun createGrainModelDescription(userId: String, sent: GrainModel) = rfc1123Format.format(Date()).let {
    GrainModelDescription(newId(), DescriptionInfo(userId, it, it, false, false), sent)
}

/** Memory implementation for Data. Only used for tests, not optimal at all */
class MemoryData(
    val users: LinkedHashMap<String, User> = linkedMapOf(),
    val grainModels: LinkedHashMap<String, GrainModelDescription> = linkedMapOf(),
    val simulations: LinkedHashMap<String, SimulationDescription> = linkedMapOf(),
    val featured: LinkedHashMap<String, FeaturedDescription> = linkedMapOf(),
    val subscriptions: LinkedHashMap<String, Subscription> = linkedMapOf(),
    val assets: LinkedHashMap<String, Asset> = linkedMapOf(),
    val assetContents: LinkedHashMap<String, ByteArray> = linkedMapOf()
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

    override fun grainModelsForUser(userId: String, offset: Int, limit: Int): ResultPage<GrainModelDescription> =
        grainModels.values.filter {
            it.info.userId == userId
        }.limit(offset, limit)

    override fun getGrainModel(id: String) = grainModels[id]

    override fun createGrainModel(userId: String, sent: GrainModel): GrainModelDescription {
        val model = createGrainModelDescription(userId, sent)
        grainModels[model.id] = model
        return model
    }

    override fun saveGrainModel(model: GrainModelDescription) {
        grainModels[model.id] = model
    }

    override fun deleteGrainModel(modelId: String) {
        getSimulationForModel(modelId).forEach { deleteSimulation(it.id) }
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

    override fun simulationsForUser(userId: String, modelId: String?, offset: Int, limit: Int): ResultPage<SimulationDescription> =
        simulations.values.filter {
            it.info.userId == userId && (modelId == null || it.modelId == modelId)
        }.limit(offset, limit)

    override fun getSimulation(id: String) = simulations[id]

    override fun createSimulation(userId: String, modelId: String, sent: Simulation): SimulationDescription {
        val result = createSimulation(userId, modelId, sent)
        simulations[result.id] = result
        return result
    }

    override fun saveSimulation(simulation: SimulationDescription) {
        simulations[simulation.id] = simulation
    }

    override fun deleteSimulation(simulationId: String) {
        simulations.remove(simulationId)
    }

    override fun getAllFeatured(offset: Int, limit: Int) =
        featured.values.toList().limit(offset, limit)

    override fun getFeatured(id: String) = featured[id]

    override fun createFeatured(simulationId: String): FeaturedDescription {
        val simulation = getSimulation(simulationId)
        val model = getGrainModel(simulation?.modelId ?: "")
        val new = if (simulation != null && model != null)
            FeaturedDescription(
                newId(), simulation.info.lastModifiedOn, simulation.thumbnailId,
                model.id, simulation.id, simulation.info.userId,
                listOf(simulation.simulation.name, model.model.name).filter { it.isNotEmpty() }.joinToString(" / "),
                listOf(
                    simulation.simulation.description,
                    model.model.description
                ).filter { it.isNotEmpty() }.joinToString("\n"),
                ""
            )
        else
            FeaturedDescription(newId(), "", null, "", simulationId, "", "", "", "")

        featured[new.id] = new
        return new
    }

    override fun deleteFeatured(featuredId: String) {
        // delete the featured
        featured.remove(featuredId)
    }

    override fun searchSimulation(query: String, offset: Int, limit: Int) = simulations.values
        .filter { it.simulation.name.contains(query) || it.simulation.description.contains(query) }
        .limit(offset, limit)

    override fun searchModel(query: String, offset: Int, limit: Int) = grainModels.values
        .filter { it.model.name.contains(query) || it.model.description.contains(query) }
        .limit(offset, limit)

    override fun subscriptionsForUser(userId: String) =
        subscriptions.values.filter { it.userId == userId }

    override fun getSubscription(id: String): Subscription? = subscriptions[id]

    override fun createSubscription(
        userId: String,
        sandbox: Boolean,
        parameters: SubscriptionParameters
    ): Subscription {
        val now = System.currentTimeMillis()
        val expiresOn = if (parameters.duration > 0) now + parameters.duration else null
        val new = Subscription(
            newId(), userId, sandbox, parameters.autoRenew, false,
            now, null, expiresOn, null,
            parameters.subscription, parameters.duration, parameters.amount, parameters.paymentMethod
        )
        subscriptions[new.id] = new
        return new
    }

    override fun saveSubscription(subscription: Subscription) {
        subscriptions[subscription.id] = subscription
    }

    override fun deleteSubscription(subscriptionId: String) {
        subscriptions.remove(subscriptionId)
    }

    override fun getAllAssets(offset: Int, limit: Int, extensions: List<String>) =
        assets.values.filter { asset -> extensions.any { asset.name.endsWith(it) } }.toList().limit(offset, limit)

    override fun assetsForUser(userId: String): List<Asset> = assets.values.filter {
        it.userId == userId
    }

    override fun getAssetContent(id: String) = assetContents[id]

    override fun createAsset(name: String, userId: String, data: ByteArray): Asset {
        val id = newId()
        val result = Asset(id, name, userId)
        assets[id] = result
        assetContents[id] = data
        return result
    }

    override fun deleteAsset(id: String) {
        assets.remove(id)
    }

    private fun <T> List<T>.limit(offset: Int, limit: Int) =
        ResultPage(this.dropLast(max(0, this.size - limit)), offset, this.size)
}
