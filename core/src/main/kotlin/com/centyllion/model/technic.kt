package com.centyllion.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

interface Ided {
    val id: String
}

data class Problem(
    val source: ModelElement,
    val property: String,
    val message: String,
)

@Serializable
data class User (
    override val id: String,
    val name: String,
    val username: String,
    val details: UserDetails? = null
): Ided

@Serializable
data class UserDetails(
    val keycloakId: String,
    val email: String,
    val tutorialDone: Boolean = false
)

interface Description: Ided {
    val name: String
    val icon: String

    val label get() = if (name.isNotEmpty()) name else id.drop(id.lastIndexOf("-") + 1)
}

@Serializable
data class DescriptionInfo(
    val user: User? = null,
    val createdOn: String = "",
    val lastModifiedOn: String = "",
    val readAccess: Boolean = true
)

@Serializable
data class GrainModelDescription(
    override val id: String,
    val info: DescriptionInfo,
    val tags: String,
    val model: GrainModel
) : Description {

    @Transient
    override val name = model.name

    @Transient
    override val icon = "boxes"
}

@Serializable
data class SimulationDescription(
    override val id: String,
    val info: DescriptionInfo,
    val modelId: String,
    val thumbnailId: String?,
    val simulation: Simulation
) : Description {

    @Transient
    override val name = simulation.name

    @Transient
    override val icon = "play"

    fun cleaned(model: GrainModelDescription): SimulationDescription {
        val new = simulation.cleaned(model.model)
        return if (new != simulation) copy(simulation = new) else this
    }
}

@Serializable
data class SimulationResultPage(
    val content: List<SimulationDescription>,
    val offset: Long,
    val totalSize: Long
)

@Serializable
data class ModelResultPage(
    val content: List<GrainModelDescription>,
    val offset: Long,
    val totalSize: Long
)
