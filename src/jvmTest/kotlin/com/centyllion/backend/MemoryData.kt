package com.centyllion.backend

import com.centyllion.model.*
import io.ktor.auth.jwt.JWTPrincipal
import org.litote.kmongo.newId
import kotlin.math.max

/** Memory implementation for Data. Only used for tests, not optimal at all */
class MemoryData(
    val users: LinkedHashMap<String, User> = linkedMapOf(),
    val grainModels: LinkedHashMap<String, GrainModelDescription> = linkedMapOf(),
    val simulations: LinkedHashMap<String, SimulationDescription> = linkedMapOf(),
    val featured: LinkedHashMap<String, FeaturedDescription> = linkedMapOf(),
    val assets: LinkedHashMap<String, Asset> = linkedMapOf(),
    val events: LinkedHashMap<String, Event> = linkedMapOf()
) : Data {

    override fun getOrCreateUserFromPrincipal(principal: JWTPrincipal) =
        users.values.find { it.keycloakId == principal.payload.subject }.let {
            if (it == null) {
                val user = createUser(principal, principal.payload.subject)
                users[user._id] = user
                user
            } else {
                it
            }
        }

    override fun getUser(id: String): User? = users[id]

    override fun saveUser(user: User) {
        users[user._id] = user
    }

    override fun publicGrainModels(max: Int) =
        grainModels.values.toList().dropLast(max(0, grainModels.size - max))

    override fun grainModelsForUser(user: User): List<GrainModelDescription> = grainModels.values.filter {
        it.info.userId == user._id
    }

    override fun getGrainModel(id: String) = grainModels[id]

    override fun createGrainModel(user: User, sent: GrainModel): GrainModelDescription {
        val model = createGrainModelDescription(user, sent)
        grainModels[model._id] = model
        return model
    }

    override fun saveGrainModel(user: User, model: GrainModelDescription) {
        grainModels[model._id] = model
    }

    override fun deleteGrainModel(user: User, model: GrainModelDescription) {
        getSimulationForModel(model._id).forEach { deleteSimulation(user, it) }
        grainModels.remove(model._id)
    }

    override fun getSimulationForModel(modelId: String): List<SimulationDescription> = simulations.values.filter {
        it.modelId == modelId
    }

    override fun getSimulation(id: String) = simulations[id]

    override fun createSimulation(user: User, modelId: String, sent: Simulation): SimulationDescription {
        val result = createSimulation(user, modelId, sent)
        simulations[result._id] = result
        return result
    }

    override fun saveSimulation(user: User, simulation: SimulationDescription) {
        simulations[simulation._id] = simulation
    }

    override fun deleteSimulation(user: User, simulation: SimulationDescription) {
        simulations.remove(simulation._id)
    }

    override fun getAllFeatured(): List<FeaturedDescription> = featured.values.toList()

    override fun getFeatured(id: String) = featured[id]

    override fun createFeatured(
        user: User, model: GrainModelDescription, simulation: SimulationDescription, author: User
    ): FeaturedDescription {
        val asset = createAsset("simulation.png", createThumbnail(model.model, simulation.simulation))
        val new = createFeaturedDescription(asset, model, simulation, author)
        featured[new._id] = new
        return new
    }

    override fun deleteFeatured(user: User, delete: FeaturedDescription) {
        // delete thumbnail asset
        if (delete.thumbnailId.isNotEmpty()) deleteAsset(delete.thumbnailId)
        // delete the featured
        featured.remove(delete._id)
    }

    override fun getAsset(id: String) = assets[id]

    override fun createAsset(name: String, data: ByteArray): Asset {
        val id = newId<Asset>().toString()
        val result = Asset(id, name, data)
        assets[id] = result
        return result
    }

    override fun deleteAsset(id: String) {
        assets.remove(id)
    }

    override fun getEvents() = events.values.toList()

    override fun insertEvent(action: Action, user: User?, collection: String, vararg arguments: String) {
        val event = createEvent(action, user, collection, arguments)
        events[event._id] = event
    }

}
