package com.centyllion.backend.data

import com.centyllion.common.SubscriptionType
import com.centyllion.model.Asset
import com.centyllion.model.DescriptionInfo
import com.centyllion.model.FeaturedDescription
import com.centyllion.model.GrainModel
import com.centyllion.model.GrainModelDescription
import com.centyllion.model.Ided
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
    val assetContents: LinkedHashMap<String, ByteArray> = linkedMapOf(),
    val backend: Data? = null
) : Data {

    private val deletedUsers = mutableSetOf<String>()
    private val deletedModels = mutableSetOf<String>()
    private val deletedSimulations = mutableSetOf<String>()
    private val deletedFeatured = mutableSetOf<String>()
    private val deletedSubscriptions = mutableSetOf<String>()
    private val deletedAssets = mutableSetOf<String>()

    /** Merges source and local lists */
    fun <T: Ided> merge(source: List<T>?, local: List<T>, deleted: Set<String>): List<T> =
        source?.let {
            local + source.filter { s -> !deleted.contains(s.id) && local.none { l -> l.id == s.id } }
        } ?: local

    override fun getAllUsers(detailed: Boolean, offset: Int, limit: Int): ResultPage<User> =
        merge(
            backend?.getAllUsers(detailed, offset, limit)?.content,
            users.values.toList().map { if (detailed) it else it.copy(details = null) },
            deletedUsers
        ).limit(offset, limit)

    override fun getOrCreateUserFromPrincipal(principal: JWTPrincipal) =
        users.values.find { it.details?.keycloakId == principal.payload.subject }.let {
            if (it == null) {
                val user = createUser(principal, principal.payload.subject)
                users[user.id] = user
                deletedUsers.remove(user.id)
                user
            } else {
                it
            }
        }

    override fun getUser(id: String, detailed: Boolean): User? = users[id]?.let {
        if (detailed) it else it.copy(details = null)
    } ?: backend?.getUser(id, detailed)

    override fun saveUser(user: User) {
        users[user.id] = user
    }

    override fun publicGrainModels(offset: Int, limit: Int) =
        grainModels.values.toList().limit(offset, limit)

    override fun grainModelsForUser(userId: String, offset: Int, limit: Int): ResultPage<GrainModelDescription> =
        merge(
            backend?.grainModelsForUser(userId, offset, limit)?.content,
            grainModels.values.filter { it.info.userId == userId },
            deletedModels
        ).limit(offset, limit)

    override fun getGrainModel(id: String) = grainModels[id] ?: backend?.getGrainModel(id)

    override fun createGrainModel(userId: String, sent: GrainModel): GrainModelDescription {
        val model = createGrainModelDescription(userId, sent)
        grainModels[model.id] = model
        deletedModels.remove(model.id)
        return model
    }

    override fun saveGrainModel(model: GrainModelDescription) {
        grainModels[model.id] = model
    }

    override fun deleteGrainModel(modelId: String) {
        simulations.values.filter { it.modelId == modelId }.forEach { deleteSimulation(it.id) }
        grainModels.remove(modelId)
        deletedModels.add(modelId)
    }

    override fun publicSimulations(modelId: String?, offset: Int, limit: Int) =
        merge(
            backend?.publicSimulations(modelId, offset, limit)?.content,
            simulations.values.filter { modelId == null || it.modelId == modelId },
            deletedSimulations
        ).limit(offset, limit)

    override fun simulationsForUser(userId: String, modelId: String?, offset: Int, limit: Int): ResultPage<SimulationDescription> =
        merge(
            backend?.simulationsForUser(userId, modelId, offset, limit)?.content,
            simulations.values.filter {
                it.info.userId == userId && (modelId == null || it.modelId == modelId)
            },
            deletedSimulations
        ).limit(offset, limit)

    override fun getSimulation(id: String) = simulations[id] ?: backend?.getSimulation(id)

    override fun createSimulation(userId: String, modelId: String, sent: Simulation): SimulationDescription {
        val result = createSimulation(userId, modelId, sent)
        simulations[result.id] = result
        deletedSimulations.remove(result.id)
        return result
    }

    override fun saveSimulation(simulation: SimulationDescription) {
        simulations[simulation.id] = simulation
    }

    override fun deleteSimulation(simulationId: String) {
        simulations.remove(simulationId)
        deletedSimulations.add(simulationId)
    }

    override fun getAllFeatured(offset: Int, limit: Int) =
        merge(
            backend?.getAllFeatured(offset, limit)?.content,
            featured.values.toList(),
            deletedFeatured
        ).limit(offset, limit)

    override fun getFeatured(id: String) = featured[id] ?: backend?.getFeatured(id)

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
        deletedFeatured.remove(new.id)
        return new
    }

    override fun deleteFeatured(featuredId: String) {
        // delete the featured
        featured.remove(featuredId)
        deletedFeatured.add(featuredId)
    }

    override fun searchSimulation(query: String, offset: Int, limit: Int) =
        merge(
            backend?.searchSimulation(query, offset, limit)?.content,
            simulations.values.filter { it.simulation.name.contains(query) || it.simulation.description.contains(query) },
            deletedSimulations
        ).limit(offset, limit)

    override fun searchModel(query: String, offset: Int, limit: Int) =
        merge(
            backend?.searchModel(query, offset, limit)?.content,
            grainModels.values.filter { it.model.name.contains(query) || it.model.description.contains(query) },
            deletedModels
        ).limit(offset, limit)

    override fun subscriptionsForUser(userId: String) =
        merge(
            backend?.subscriptionsForUser(userId),
            subscriptions.values.filter { it.userId == userId },
            deletedSubscriptions
        )

    override fun getSubscription(id: String): Subscription? = subscriptions[id] ?: backend?.getSubscription(id)

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
        deletedSubscriptions.remove(new.id)
        return new
    }

    override fun saveSubscription(subscription: Subscription) {
        subscriptions[subscription.id] = subscription
    }

    override fun deleteSubscription(subscriptionId: String) {
        subscriptions.remove(subscriptionId)
        deletedSubscriptions.add(subscriptionId)
    }

    override fun getAllAssets(offset: Int, limit: Int, extensions: List<String>) =
        merge(
            backend?.getAllAssets(offset, limit, extensions)?.content,
            assets.values.filter { asset -> extensions.any { asset.name.endsWith(it) } }.toList(),
            deletedAssets
        ).limit(offset, limit)

    override fun assetsForUser(userId: String): List<Asset> =
        merge(
            backend?.assetsForUser(userId),
            assets.values.filter { it.userId == userId },
            deletedAssets
        )

    override fun getAssetContent(id: String) = assetContents[id] ?: backend?.getAssetContent(id)

    override fun createAsset(name: String, userId: String, data: ByteArray): Asset {
        val id = newId()
        val result = Asset(id, name, userId)
        assets[id] = result
        assetContents[id] = data
        deletedAssets.remove(id)
        return result
    }

    override fun deleteAsset(id: String) {
        assets.remove(id)
        assetContents.remove(id)
        deletedAssets.add(id)
    }

    private fun <T> List<T>.limit(offset: Int, limit: Int) =
        ResultPage(this.dropLast(max(0, this.size - limit)), offset, this.size)
}