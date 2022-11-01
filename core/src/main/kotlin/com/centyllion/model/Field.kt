package com.centyllion.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.util.*
import kotlin.math.pow

@Serializable
data class Field(
    override val uuid: String = UUID.randomUUID().toString(),
    val id: Int = 0,
    override val name: String = "",
    val color: String = "SkyBlue",
    val invisible: Boolean = false,
    override val description: String = "",
    val speed: Float = 0.8f,
    val halfLife: Int = 10,
    val allowedDirection: Set<Direction> = Direction.default
): ModelElement {
    /** Label for grain */
    fun label(long: Boolean = false) = when {
        long && description.isNotEmpty() -> description
        name.isNotEmpty() -> name
        else -> "$id"
    }

    @Transient
    val deathProbability = if (halfLife > 0) 1f - 2f.pow(-1f / halfLife) else 0f

    @Transient
    val oppositeDirections = allowedDirection.map { it.opposite }
}