package com.centyllion.model

import kotlinx.serialization.Serializable

@Serializable
data class Reaction(
    val reactiveId: Int = -1,
    val productId: Int = -1,
    val sourceReactive: Int = -1,
    val allowedDirection: Set<Direction> = Direction.default
)