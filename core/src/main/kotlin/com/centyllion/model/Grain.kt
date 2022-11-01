package com.centyllion.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.util.*
import kotlin.math.pow

@Serializable
data class Grain(
    override val uuid: String = UUID.randomUUID().toString(),
    val id: Int = 0,
    override val name: String = "",
    val color: String = "red",
    val icon: String = "SquareFull",
    val invisible: Boolean = false,
    val size: Double = 1.0,
    override val description: String = "",
    val halfLife: Int = 0,
    val movementProbability: Double = 1.0,
    val allowedDirection: Set<Direction> = Direction.default,
    val fieldProductions: Map<Int, Float> = emptyMap(),
    val fieldInfluences: Map<Int, Float> = emptyMap(),
    val fieldPermeable: Map<Int, Float> = emptyMap()
): ModelElement {

    @Transient
    val iconName = icon.replace("-", "").lowercase()
    /** Label for grain */
    fun label(long: Boolean = false) = when {
        long && description.isNotEmpty() -> description
        name.isNotEmpty() -> name
        else -> "$id"
    }

    /** True if an agent of this Grain can move */
    @Transient
    val canMove = movementProbability > 0.0 && allowedDirection.isNotEmpty()

    @Transient
    val deathProbability = if (halfLife > 0) 1.0 - 2.0.pow(-1.0 / halfLife) else 0.0

    @Transient
    val iconString = solidIconNames[icon] ?: "\uf45c"

    fun updateFieldProduction(id: Int, value: Float): Grain {
        val newFields = fieldProductions.toMutableMap()
        newFields[id] = value
        return copy(fieldProductions = newFields)
    }

    fun updateFieldInfluence(id: Int, value: Float): Grain {
        val newFields = fieldInfluences.toMutableMap()
        newFields[id] = value
        return copy(fieldInfluences = newFields)
    }

    fun updateFieldPermeable(id: Int, value: Float): Grain {
        val newFields = fieldPermeable.toMutableMap()
        newFields[id] = value
        return copy(fieldPermeable = newFields)
    }

    fun moveBehaviour() =
        if (canMove) Behaviour(
            "Move ${label()}", probability = movementProbability, mainReactiveId = id, sourceReactive = -1,
            reaction = listOf(Reaction(productId = id, sourceReactive = 0, allowedDirection = allowedDirection)),
            fieldInfluences = fieldInfluences
        )
        else null

}