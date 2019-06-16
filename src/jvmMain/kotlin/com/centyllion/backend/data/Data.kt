package com.centyllion.backend.data

import com.centyllion.common.SubscriptionType
import com.centyllion.model.*
import io.ktor.auth.jwt.JWTPrincipal

interface Data {
    fun getAllUsers(detailed: Boolean, offset: Int = 0, limit: Int = 20): ResultPage<User>
    fun getOrCreateUserFromPrincipal(principal: JWTPrincipal): User
    fun getUser(id: String, detailed: Boolean): User?
    fun saveUser(user: User)

    fun publicGrainModels(offset: Int = 0, limit: Int = 20): ResultPage<GrainModelDescription>
    fun searchModel(query: String, offset: Int = 0, limit: Int = 20): ResultPage<GrainModelDescription>
    fun grainModelsForUser(userId: String): List<GrainModelDescription>
    fun getGrainModel(id: String): GrainModelDescription?
    fun createGrainModel(userId: String, sent: GrainModel): GrainModelDescription
    fun saveGrainModel(model: GrainModelDescription)
    fun deleteGrainModel(modelId: String)

    fun publicSimulations(offset: Int = 0, limit: Int = 20): ResultPage<SimulationDescription>
    fun searchSimulation(query: String, offset: Int = 0, limit: Int = 20): ResultPage<SimulationDescription>
    fun getSimulationForModel(modelId: String): List<SimulationDescription>
    fun getSimulation(id: String): SimulationDescription?
    fun createSimulation(userId: String, modelId: String, sent: Simulation): SimulationDescription
    fun saveSimulation(simulation: SimulationDescription)
    fun deleteSimulation(simulationId: String)

    fun getAllFeatured(offset: Int = 0, limit: Int = 20): ResultPage<FeaturedDescription>
    fun getFeatured(id: String): FeaturedDescription?
    fun createFeatured(simulationId: String): FeaturedDescription
    fun deleteFeatured(featuredId: String)

    fun subscriptionsForUser(userId: String, all: Boolean): List<Subscription>
    fun getSubscription(id: String): Subscription?
    fun createSubscription(
        userId: String, sandbox: Boolean, duration: Int,
        type: SubscriptionType, amount: Double, paymentMethod: String
    ): Subscription
    fun saveSubscription(subscription: Subscription)
    fun deleteSubscription(subscriptionId: String)


    fun getAsset(id: String): Asset?
    fun createAsset(name: String, data: ByteArray): Asset
    fun deleteAsset(id: String)
}
