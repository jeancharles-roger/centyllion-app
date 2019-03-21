package com.centyllion.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.math.pow

enum class Direction {
    Left, Right, Up, Down, Front, Back
}

val defaultDirection = setOf(Direction.Left, Direction.Up, Direction.Right, Direction.Down)

enum class Figure {
    Square, Triangle, Disk, Diamond, Star
}

enum class Operator {
    Equals, NotEquals, LessThan, LessThanOrEquals, GreaterThan, GreaterThanOrEquals
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
    val figure: Figure = Figure.Square,
    val description: String = "",
    val halfLife: Int = 0,
    val movementProbability: Double = 1.0,
    val allowedDirection: Set<Direction> = defaultDirection
) {
    /** True if an agent of this Grain can move */
    @Transient
    val canMove = movementProbability > 0.0 && allowedDirection.isNotEmpty()

    @Transient
    val deathProbability = if (halfLife > 0.0) 1.0 - 2.0.pow(-1.0 / halfLife) else 0.0

    @Transient
    val valid
        get() = id > 0 && name.isNotBlank()
}

@Serializable
data class Reaction(
    val reactiveId: Int = -1,
    val productId: Int = -1,
    val transform: Boolean = false,
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
    // TODO inline main reaction
    val mainReaction: Reaction = Reaction(),
    val reaction: List<Reaction> = emptyList()
) {

    /** Is behavior applicable for given [grain], [age] and [neighbours] ? */
    fun applicable(grain: Grain, age: Int, neighbours: Map<Direction, Int>): Boolean =
        mainReaction.reactiveId == grain.id && agePredicate.check(age) &&
                reaction.fold(true) { a, r ->
                    a && r.allowedDirection.any {
                        (neighbours[it] == r.reactiveId)
                    }
                }

    fun validForModel(model: GrainModel) = name.isNotBlank() && probability >= 0.0 && probability <= 1.0 &&
            mainReaction.reactiveId >= 0 && mainReaction.validForModel(model) &&
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

    /** Main reactive grains are all the grains that are main component for a behaviour */
    @Transient
    val mainReactiveGrains get() = behaviours.mapNotNull { indexedGrains[it.mainReaction.reactiveId] }.toSet()

    @Transient
    val valid
        get() =
            name.isNotBlank() &&
                    grains.fold(true) { a, g -> a && g.valid } &&
                    behaviours.fold(true) { a, g -> a && g.validForModel(this) }

}

@Serializable
data class Simulation(
    val width: Int = 100,
    val height: Int = 100,
    val depth: Int = 1,
    val initialAgents: Array<Int> = Array(width * height * depth) { -1 },
    val agents: Array<Int> = initialAgents.copyOf(),
    val ages: Array<Int> = Array(initialAgents.size) { -1 }
) {

    @Transient
    val levelSize = width * height

    @Transient
    val dataSize = levelSize * depth


    /** Move [index] on given [direction] of [step] cases, whatever the index, it will always remains inside the simulation. */
    fun moveIndex(index: Int, direction: Direction, step: Int = 1): Int {
        // TODO find a faster way to move index
        var z = index / levelSize
        val yRest = index - z * levelSize
        var y = yRest / width
        var x = yRest - y * width

        when (direction) {
            Direction.Left -> x = (x - step) % width
            Direction.Right -> x = (x + step) % width
            Direction.Up -> y = (y - step) % height
            Direction.Down -> y = (y + step) % height
            Direction.Front -> z = (z - step) % depth
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

    fun reset() {
        initialAgents.copyInto(agents)
        for (i in 0 until ages.size) {
            ages[i] = if (agents[i] != -1) 0 else -1
        }
    }

    fun saveState() {
        agents.copyInto(initialAgents)
    }

    fun indexIsFree(index: Int) = agents[index] < 0

    fun idAtIndex(index: Int) = agents[index]

    fun ageAtIndex(index: Int) = ages[index]

    fun ageGrain(index: Int) {
        ages[index] += 1
    }

    fun transform(sourceIndex: Int, targetIndex: Int, newId: Int?, keepAge: Boolean) {
        val age = ages[sourceIndex]
        agents[sourceIndex] = -1
        ages[sourceIndex] = -1
        agents[targetIndex] = newId ?: -1
        ages[targetIndex] = when {
            newId != null && keepAge -> age
            newId != null -> 0
            else -> -1
        }
    }

    fun setIdAtIndex(index: Int, id: Int) {
        agents[index] = id
        ages[index] = 0
    }

    fun neighbours(index: Int): Map<Direction, Int> = Direction.values().map { it to agents[moveIndex(index, it)] }.toMap()

    fun grainsCounts(): Map<Int, Int> {
        val result = mutableMapOf<Int, Int>()
        for (i in agents) {
            if (i >= 0) {
                result[i] = 1 + (result[i] ?: 0)
            }
        }
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Simulation

        if (width != other.width) return false
        if (height != other.height) return false
        if (depth != other.depth) return false
        if (!initialAgents.contentEquals(other.initialAgents)) return false
        if (!agents.contentEquals(other.agents)) return false
        if (!ages.contentEquals(other.ages)) return false
        if (levelSize != other.levelSize) return false
        if (dataSize != other.dataSize) return false

        return true
    }

    override fun hashCode(): Int {
        var result = width
        result = 31 * result + height
        result = 31 * result + depth
        result = 31 * result + initialAgents.contentHashCode()
        result = 31 * result + agents.contentHashCode()
        result = 31 * result + ages.contentHashCode()
        result = 31 * result + levelSize
        result = 31 * result + dataSize
        return result
    }

    @Transient
    val valid
        get() = width > 0 && height > 0 && depth > 0

}

@Serializable
data class GrainModelDescription(
    val _id: String,
    val userId: String,
    val previousId: String?,
    val nextId: String?,
    val date: String,
    val model: GrainModel
)

@Serializable
data class SimulationDescription(
    val _id: String,
    val previousId: String?,
    val nextId: String?,
    val date: String,
    val modelId: String,
    val simulation: Simulation
)
