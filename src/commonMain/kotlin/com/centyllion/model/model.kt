package com.centyllion.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.math.pow

val versions = mapOf(
    GrainModelDescription.serializer() to 2
)

fun version(serializer: KSerializer<*>) = versions[serializer] ?: 0

val emptyModel = GrainModel("")
val emptyDescription = DescriptionInfo()
val emptyGrainModelDescription = GrainModelDescription("", info = emptyDescription, model = emptyModel)
val emptySimulation = Simulation("")
val emptySimulationDescription =
    SimulationDescription("", info = emptyDescription, modelId = "", thumbnailId = null, simulation = emptySimulation)

enum class Direction {
    Left, Right, Up, Down, Front, Back
}

val defaultDirection = setOf(Direction.Left, Direction.Up, Direction.Right, Direction.Down)

enum class Operator(val label: String) {
    Equals("="), NotEquals("!="), LessThan("<"), LessThanOrEquals("<="), GreaterThan(">"), GreaterThanOrEquals(">=")
}

@Serializable
data class Predicate<C : Comparable<C>>(
    val op: Operator,
    val constant: C
) {
    fun check(value: C) = when (op) {
        Operator.Equals -> value == constant
        Operator.NotEquals -> value != constant
        Operator.LessThan -> value < constant
        Operator.LessThanOrEquals -> value <= constant
        Operator.GreaterThan -> value > constant
        Operator.GreaterThanOrEquals -> value >= constant
    }
}

@Serializable
data class Grain(
    val id: Int = 0,
    val name: String = "",
    val color: String = "red",
    val icon: String = "square-full",
    val size: Double = 1.0,
    val description: String = "",
    val halfLife: Int = 0,
    val movementProbability: Double = 1.0,
    val allowedDirection: Set<Direction> = defaultDirection
) {
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
    val deathProbability = if (halfLife > 0.0) 1.0 - 2.0.pow(-1.0 / halfLife) else 0.0

    @Transient
    val iconString = solidIconNames[icon]

    @Transient
    val valid
        get() = id > 0 && name.isNotBlank()

    fun moveBehaviour() =
        if (canMove) Behaviour(
            "Move ${label()}", probability = movementProbability, mainReactiveId = id, sourceReactive = -1,
            reaction = listOf(Reaction(productId = id, sourceReactive = 0, allowedDirection = allowedDirection))
        )
        else null

}

@Serializable
data class Reaction(
    val reactiveId: Int = -1,
    val productId: Int = -1,
    val sourceReactive: Int = -1,
    val allowedDirection: Set<Direction> = defaultDirection
) {
    fun validForModel(model: GrainModel) = model.indexedGrains.containsKey(reactiveId) &&
            if (productId >= 0) model.indexedGrains.containsKey(productId) else true
}

@Serializable
data class Behaviour(
    val name: String = "",
    val description: String = "",
    val probability: Double = 1.0,
    val agePredicate: Predicate<Int> = Predicate(Operator.GreaterThanOrEquals, 0),
    val mainReactiveId: Int = -1, val mainProductId: Int = -1, val sourceReactive: Int = -1,
    val reaction: List<Reaction> = emptyList()
) {

    /** Is behavior applicable for given [grain], [age] and [neighbours] ? */
    fun applicable(grain: Grain, age: Int, neighbours: List<Pair<Direction, Agent>>): Boolean =
        mainReactiveId == grain.id && agePredicate.check(age) &&
                reaction.fold(true) { a, r ->
                    a && r.allowedDirection.any { d ->
                        neighbours.any { it.first == d && it.second.id == r.reactiveId }
                    }
                }

    fun usedGrains(model: GrainModel) =
        (reaction.flatMap { listOf(it.reactiveId, it.productId) } + mainReactiveId + mainProductId)
            .filter { it >= 0 }.mapNotNull { model.indexedGrains[it] }.toSet()

    fun validForModel(model: GrainModel) = name.isNotBlank() && probability >= 0.0 && probability <= 1.0 &&
            mainReactiveId >= 0 &&
            reaction.fold(true) { a, r -> a && r.validForModel(model) }
}

@Serializable
data class Position(
    val x: Int, val y: Int, val z: Int = 0
) {
    fun move(direction: Direction, step: Int = 1) = when (direction) {
        Direction.Left -> copy(x = x + step)
        Direction.Right -> copy(x = x - step)
        Direction.Up -> copy(y = y - step)
        Direction.Down -> copy(y = y + step)
        Direction.Front -> copy(z = z - step)
        Direction.Back -> copy(z = z + step)
    }
}

@Serializable
data class GrainModel(
    val name: String = "",
    val description: String = "",
    val grains: List<Grain> = emptyList(),
    val behaviours: List<Behaviour> = emptyList()
) {
    @Transient
    val indexedGrains: Map<Int, Grain> = grains.map { it.id to it }.toMap()

    fun availableGrainName(prefix: String = "grain"): String = grains.map { it.name }.toSet().let {
        if (!it.contains(prefix)) return prefix
        for (i in 1..grains.size) {
            "$prefix $i".let { name -> if (!it.contains(name)) return name }
        }
        return "$prefix ${grains.size + 1}"
    }

    fun availableGrainId(): Int = grains.map { it.id }.toSet().let {
        for (i in 0 until grains.size) {
            if (!it.contains(i)) return i
        }
        return grains.size
    }

    fun availableGrainColor(): String = (colorNames.keys - grains.map { it.color }).let {
        if (it.isEmpty()) "red" else it.random()
    }

    fun newGrain() = Grain(availableGrainId(), availableGrainName(), availableGrainColor())

    @Transient
    val valid
        get() =
            name.isNotBlank() &&
                    grains.fold(true) { a, g -> a && g.valid } &&
                    behaviours.fold(true) { a, g -> a && g.validForModel(this) }

}

fun emptyList(size: Int): List<Int> = ArrayList<Int>(size).apply { repeat(size) { add(-1) } }

@Serializable
data class Simulation(
    val name: String = "",
    val description: String = "",
    val width: Int = 100,
    val height: Int = 100,
    val depth: Int = 1,
    val agents: List<Int> = emptyList(width * height * depth)
) {
    @Transient
    val levelSize = width * height

    @Transient
    val dataSize = levelSize * depth

    @Transient
    val valid
        get() = width > 0 && height > 0 && depth > 0

    /** Move [index] on given [direction] of [step] cases, whatever the index, it will always remains inside the simulation. */
    fun moveIndex(index: Int, direction: Direction, step: Int = 1): Int {
        // TODO find a faster way to move index
        var z = index / levelSize
        val yRest = index - z * levelSize
        var y = yRest / width
        var x = yRest - y * width

        when (direction) {
            Direction.Left -> x = (x + width - step) % width
            Direction.Right -> x = (x + step) % width
            Direction.Up -> y = (y + height - step) % height
            Direction.Down -> y = (y + step) % height
            Direction.Front -> z = (z + depth - step) % depth
            Direction.Back -> z = (z + step) % depth
        }

        if (x < 0) x += width
        if (y < 0) y += height
        if (z < 0) z += depth

        return z * levelSize + y * width + x
    }

    /** Transform given [position] to index. */
    fun toIndex(position: Position) = toIndex(position.x, position.y, position.z)

    fun toIndex(x: Int, y: Int, z: Int = 0) = z * (height * width) + y * width + x

    fun indexInside(index: Int) = index in 0 until dataSize

    /** Transforms index to position, only to be used for printing, it's slow */
    fun toPosition(index: Int): Position {
        val zDelta = height * width
        val z = index / zDelta
        val yRest = index - z * zDelta
        val y = yRest / width
        return Position(yRest - y * width, y, z)
    }

    fun positionInside(position: Position) =
        position.z in 0 until depth && position.y in 0 until height && position.x in 0 until width

    fun positionInside(x: Int, y: Int, z: Int = 0) =
        z in 0 until depth && y in 0 until height && x in 0 until width

}

enum class Access {
    Read, Copy
}

@Serializable
data class DescriptionInfo(
    val userId: String = "",
    val createdOn: String = "",
    val lastModifiedOn: String = "",
    val readAccess: Boolean = false,
    val cloneAccess: Boolean = false
)

@Serializable
data class GrainModelDescription(
    val id: String,
    val info: DescriptionInfo,
    val model: GrainModel
)

@Serializable
data class SimulationDescription(
    val id: String,
    val info: DescriptionInfo,
    val modelId: String,
    val thumbnailId: String?,
    val simulation: Simulation
)

@Serializable
data class FeaturedDescription(
    val id: String,
    val date: String,
    val thumbnailId: String?,
    val modelId: String,
    val simulationId: String,
    val authorId: String,
    val name: String,
    val description: String,
    val authorName: String
)

fun emptyFeatured(modelId: String, simulationId: String, authorId: String) = FeaturedDescription(
    "", "", null, modelId, simulationId, authorId, "", "", ""
)
