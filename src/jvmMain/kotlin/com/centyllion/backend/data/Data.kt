package com.centyllion.backend.data

import com.centyllion.model.Asset
import com.centyllion.model.FeaturedDescription
import com.centyllion.model.GrainModel
import com.centyllion.model.GrainModelDescription
import com.centyllion.model.ResultPage
import com.centyllion.model.Simulation
import com.centyllion.model.SimulationDescription
import com.centyllion.model.Subscription
import com.centyllion.model.SubscriptionParameters
import com.centyllion.model.User
import io.ktor.auth.jwt.JWTPrincipal

interface Data {
    fun getAllUsers(detailed: Boolean, offset: Int = 0, limit: Int = 20): ResultPage<User>
    fun getOrCreateUserFromPrincipal(principal: JWTPrincipal): User
    fun getUserFromKeycloakId(keycloakId: String, detailed: Boolean): User?
    fun getUser(id: String, detailed: Boolean): User?
    fun saveUser(user: User)

    fun publicGrainModels(offset: Int = 0, limit: Int = 20): ResultPage<GrainModelDescription>
    fun searchModel(query: String, offset: Int = 0, limit: Int = 20): ResultPage<GrainModelDescription>
    fun grainModelsForUser(userId: String, offset: Int = 0, limit: Int = 20): ResultPage<GrainModelDescription>
    fun getGrainModel(id: String): GrainModelDescription?
    fun createGrainModel(userId: String, sent: GrainModel): GrainModelDescription
    fun saveGrainModel(model: GrainModelDescription)
    fun deleteGrainModel(modelId: String)

    fun publicSimulations(modelId: String?, offset: Int = 0, limit: Int = 20): ResultPage<SimulationDescription>
    fun simulationsForUser(userId: String, modelId: String?, offset: Int = 0, limit: Int = 20): ResultPage<SimulationDescription>
    fun searchSimulation(query: String, offset: Int = 0, limit: Int = 20): ResultPage<SimulationDescription>
    fun getSimulation(id: String): SimulationDescription?
    fun createSimulation(userId: String, modelId: String, sent: Simulation): SimulationDescription
    fun saveSimulation(simulation: SimulationDescription)
    fun deleteSimulation(simulationId: String)

    fun getAllFeatured(offset: Int = 0, limit: Int = 20): ResultPage<FeaturedDescription>
    fun getFeatured(id: String): FeaturedDescription?
    fun createFeatured(simulationId: String): FeaturedDescription
    fun deleteFeatured(featuredId: String)

    fun subscriptionsForUser(userId: String): List<Subscription>
    fun getSubscription(id: String): Subscription?
    fun createSubscription(userId: String, sandbox: Boolean, parameters: SubscriptionParameters): Subscription
    fun saveSubscription(subscription: Subscription)
    fun deleteSubscription(subscriptionId: String)

    fun getAllAssets(offset: Int = 0, limit: Int = 20, extensions: List<String>): ResultPage<Asset>
    fun assetsForUser(userId: String): List<Asset>
    fun getAssetContent(id: String): ByteArray?
    fun createAsset(name: String, userId: String, data: ByteArray): Asset
    fun deleteAsset(id: String)
}
