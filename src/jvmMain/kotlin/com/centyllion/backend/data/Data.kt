package com.centyllion.backend.data

import com.centyllion.model.*
import io.ktor.auth.jwt.JWTPrincipal

interface Data {
    fun getAllUsers(detailed: Boolean, offset: Int = 0, limit: Int = 20): ResultPage<User>
    fun getOrCreateUserFromPrincipal(principal: JWTPrincipal): User
    fun getUser(id: String, detailed: Boolean): User?
    fun saveUser(user: User)

    fun publicGrainModels(offset: Int = 0, limit: Int = 20): ResultPage<GrainModelDescription>
    fun grainModelsForUser(user: User): List<GrainModelDescription>
    fun getGrainModel(id: String): GrainModelDescription?
    fun createGrainModel(user: User, sent: GrainModel): GrainModelDescription
    fun saveGrainModel(user: User, model: GrainModelDescription)
    fun deleteGrainModel(user: User, modelId: String)

    fun publicSimulations(offset: Int = 0, limit: Int = 20): ResultPage<SimulationDescription>
    fun getSimulationForModel(modelId: String): List<SimulationDescription>
    fun getSimulation(id: String): SimulationDescription?
    fun createSimulation(user: User, modelId: String, sent: Simulation): SimulationDescription
    fun saveSimulation(user: User, simulation: SimulationDescription)
    fun deleteSimulation(user: User, simulationId: String)

    fun getAllFeatured(offset: Int = 0, limit: Int = 20): ResultPage<FeaturedDescription>
    fun getFeatured(id: String): FeaturedDescription?
    fun createFeatured(
        user: User,
        model: GrainModelDescription,
        simulation: SimulationDescription,
        author: User
    ): FeaturedDescription

    fun deleteFeatured(user: User, featuredId: String)

    fun searchModel(query: String, offset: Int = 0, limit: Int = 20): ResultPage<GrainModelDescription>

    fun searchSimulation(query: String, offset: Int = 0, limit: Int = 20): ResultPage<SimulationDescription>

    fun getAsset(id: String): Asset?
    fun createAsset(name: String, data: ByteArray): Asset
    fun deleteAsset(id: String)
}