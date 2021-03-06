package com.centyllion.backend.data

import com.centyllion.model.Asset
import com.centyllion.model.CollectionInfo
import com.centyllion.model.DescriptionInfo
import com.centyllion.model.FeaturedDescription
import com.centyllion.model.GrainModel
import com.centyllion.model.GrainModelDescription
import com.centyllion.model.Ided
import com.centyllion.model.ResultPage
import com.centyllion.model.Simulation
import com.centyllion.model.SimulationDescription
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
    return User(newId(), name, username, UserDetails(keycloakId, email))
}

fun createGrainModelDescription(user: User?, sent: GrainModel) = rfc1123Format.format(Date()).let {
    GrainModelDescription(newId(), DescriptionInfo(user, it, it), "", sent)
}

fun createSimulationDescription(user: User?, modelId: String, sent: Simulation) = rfc1123Format.format(Date()).let {
    SimulationDescription(newId(),  DescriptionInfo(user, it, it), modelId, null, sent)
}

/** Memory implementation for Data. Only used for tests, not optimal at all */
class MemoryData(
    val users: LinkedHashMap<String, User> = linkedMapOf(),
    val grainModels: LinkedHashMap<String, GrainModelDescription> = linkedMapOf(),
    val simulations: LinkedHashMap<String, SimulationDescription> = linkedMapOf(),
    val featured: LinkedHashMap<String, FeaturedDescription> = linkedMapOf(),
    val assets: LinkedHashMap<String, Asset> = linkedMapOf(),
    val assetContents: LinkedHashMap<String, ByteArray> = linkedMapOf(),
    val backend: Data? = null
) : Data {

    private val deletedUsers = mutableSetOf<String>()
    private val deletedModels = mutableSetOf<String>()
    private val deletedSimulations = mutableSetOf<String>()
    private val deletedFeatured = mutableSetOf<String>()
    private val deletedAssets = mutableSetOf<String>()

    /** Merges source and local Ided lists */
    fun <T: Ided> mergeIded(source: ResultPage<T>?, local: List<T>, deleted: Set<String>, offset: Long, limit: Int) =
        source?.let {
            val all = local + source.content.filter { s -> !deleted.contains(s.id) && local.none { l -> l.id == s.id } }
            ResultPage(all.limit(offset, limit).content, offset, it.totalSize)
        } ?: local.limit(offset, limit)

    /** Merges source and local lists */
    fun <T: Ided> mergeIded(source: List<T>?, local: List<T>, deleted: Set<String>): List<T> =
        source?.let { local + source.filter { s -> !deleted.contains(s.id) && local.none { l -> l.id == s.id } } } ?: local

    /** Merges source and local string lists */
    fun mergeString(source: ResultPage<String>?, local: List<String>, deleted: Set<String>, offset: Long, limit: Int) =
        source?.let {
            val all = local + source.content.filter { s -> !deleted.contains(s) && local.none { l -> l == s } }
            ResultPage(all.limit(offset, limit).content, offset, it.totalSize)
        } ?: local.limit(offset, limit)

    override fun usersInfo() = backend?.usersInfo() ?: CollectionInfo(users.size.toLong(), 0, 0)

    override fun getAllUsers(detailed: Boolean, offset: Long, limit: Int): ResultPage<User> =
        mergeIded(
            backend?.getAllUsers(detailed, offset, limit),
            users.values.toList().map { if (detailed) it else it.copy(details = null) },
            deletedUsers, offset, limit
        )

    override fun getOrCreateUserFromPrincipal(principal: JWTPrincipal): User {
        val user =
            getUserFromKeycloakId(principal.payload.subject, true) ?:
            backend?.getUserFromKeycloakId(principal.payload.subject, true)

        return if (user == null) {
            val newUser = createUser(principal, principal.payload.subject)
            users[newUser.id] = newUser
            deletedUsers.remove(newUser.id)
            newUser
        } else {
            user
        }
    }

    override fun getUserFromKeycloakId(keycloakId: String, detailed: Boolean): User? =
        users.values.find { it.details?.keycloakId == keycloakId }?.let {
            if (detailed) it else it.copy(details = null)
        }

    override fun getUser(id: String, detailed: Boolean): User? = users[id]?.let {
        if (detailed) it else it.copy(details = null)
    } ?: backend?.getUser(id, detailed)

    override fun saveUser(user: User) {
        users[user.id] = user
    }

    override fun grainModelsInfo() = backend?.grainModelsInfo() ?: CollectionInfo(grainModels.size.toLong(), 0, 0)

    override fun grainModels(callerId: String?, userId: String?, offset: Long, limit: Int): ResultPage<GrainModelDescription> {
        return mergeIded(
            backend?.grainModels(callerId, userId, offset, limit),
            grainModels.values.filter { model ->
                userId?.let { it == model.info.user?.id } ?: true &&
                (model.info.readAccess || if (callerId != null) model.info.user?.id == callerId else false)
            },
            deletedModels, offset, limit
        )
    }

    override fun getGrainModel(id: String) = grainModels[id] ?: backend?.getGrainModel(id)

    override fun createGrainModel(userId: String, sent: GrainModel): GrainModelDescription {
        val user = getUser(userId, false)
        val model = createGrainModelDescription(user, sent)
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

    override fun simulationsInfo() = backend?.simulationsInfo() ?: CollectionInfo(simulations.size.toLong(), 0, 0)

    override fun simulations(callerId: String?, userId: String?, modelId: String?, offset: Long, limit: Int): ResultPage<SimulationDescription> {
        return mergeIded(
            backend?.simulations(callerId, userId, modelId, offset, limit),
            simulations.values.filter { simulation ->
                userId?.let { it == simulation.info.user?.id } ?: true &&
                modelId?.let { it == simulation.modelId } ?: true &&
                (simulation.info.readAccess || if (callerId != null) simulation.info.user?.id == callerId else false)
            },
            deletedSimulations, offset, limit
        )
    }

    override fun simulationsSelection(offset: Long, limit: Int): ResultPage<SimulationDescription> =
        simulations(null, null, null, offset, limit)

    override fun getSimulation(id: String) = simulations[id] ?: backend?.getSimulation(id)

    override fun createSimulation(userId: String, modelId: String, sent: Simulation): SimulationDescription {
        val user = getUser(userId, false)
        val result = createSimulationDescription(user, modelId, sent)
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

    override fun getAllFeatured(offset: Long, limit: Int) =
        mergeIded(
            backend?.getAllFeatured(offset, limit),
            featured.values.toList(),
            deletedFeatured, offset, limit
        )

    override fun getFeatured(id: String) = featured[id] ?: backend?.getFeatured(id)

    override fun createFeatured(simulationId: String): FeaturedDescription {
        val simulation = getSimulation(simulationId)
        val model = getGrainModel(simulation?.modelId ?: "")
        val new = if (simulation != null && model != null)
            FeaturedDescription(
                newId(), simulation.info.lastModifiedOn, simulation.thumbnailId,
                model.id, simulation.id, simulation.info.user?.id ?: "",
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

    override fun searchSimulation(query: String, offset: Long, limit: Int) =
        mergeIded(
            backend?.searchSimulation(query, offset, limit),
            simulations.values.filter { it.simulation.name.contains(query) || it.simulation.description.contains(query) },
            deletedSimulations, offset, limit
        )

    override fun modelTags(userId: String?, offset: Long, limit: Int): ResultPage<String> {
        val words = grainModels.values
            .filter { userId == null || it.info.user?.id == userId}
            .map { it.tags.split(" ").filter(String::isNotBlank).map(String::trim) }
            .flatten()
        val counts = mutableMapOf<String, Int>()
        words.forEach { counts[it] = 1 + (counts[it] ?: 0) }
        return mergeString(
            backend?.modelTags(userId, offset, limit),
            counts.keys.toList().sortedByDescending { counts[it] ?: 0 },
            emptySet(), offset, limit
        )
    }

    override fun searchModel(query: String, tags: List<String>, offset: Long, limit: Int) =
        mergeIded(
            backend?.searchModel(query, tags, offset, limit),
            grainModels.values.filter {
                it.info.readAccess &&
                tags.all { tag -> it.tags.contains(tag) }        &&
                (it.model.name.contains(query) || it.model.description.contains(query))
            },
            deletedModels, offset, limit
        )

    override fun getAllAssets(offset: Long, limit: Int, extensions: List<String>) =
        mergeIded(
            backend?.getAllAssets(offset, limit, extensions),
            assets.values.filter { asset -> extensions.any { asset.name.endsWith(it) } }.toList(),
            deletedAssets, offset, limit
        )

    override fun assetsForUser(userId: String): List<Asset> =
        mergeIded(
            backend?.assetsForUser(userId),
            assets.values.filter { it.userId == userId },
            deletedAssets
        )

    override fun getAsset(id: String) = assets[id] ?: backend?.getAsset(id)

    override fun getAssetContent(id: String) = assetContents[id] ?: backend?.getAssetContent(id)

    override fun createAsset(name: String, userId: String, data: ByteArray): Asset {
        val id = newId()
        val entries = if (name.endsWith(".zip")) listZipEntries(data) else emptyList<String>()
        val result = Asset(id, name, entries, userId)
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

    private fun <T> List<T>.limit(offset: Long, limit: Int) =
        ResultPage(this.dropLast(max(0, this.size - limit)), offset, this.size.toLong())
}
