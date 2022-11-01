package com.centyllion.model

import kotlinx.serialization.Serializable

@Serializable
data class SimulationSettings(
    val showGrid: Boolean = true,
    val gridTextureUrl: String? = null,
    val backgroundColor: String? = null
)
