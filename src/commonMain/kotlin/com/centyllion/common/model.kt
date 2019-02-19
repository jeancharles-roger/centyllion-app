package com.centyllion.common

import kotlinx.serialization.Serializable

enum class Direction {
    Left, Right, Up, Down, Front, Back
}

val defaultDirection = listOf(Direction.Left, Direction.Up, Direction.Right, Direction.Down)

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
    val id: Int,
    val name: String,
    val color: String,
    val figure: Figure = Figure.Square,
    val description: String = "",
    val halfLife: Double = 0.0,
    val movementProbability: Double = 1.0,
    val allowedDirection: List<Direction> = defaultDirection
) {
    /** True if an agent of this Grain can move */
    val canMove = movementProbability > 0.0 && allowedDirection.isNotEmpty()

    val valid get() = id > 0 && name.isNotBlank()
}

@Serializable
data class Reaction(
    val reactiveId: Int,
    val productId: Int?,
    val transform: Boolean = false,
    val allowedDirection: List<Direction> = defaultDirection
) {
    fun validForModel(model: Model) = model.indexedGrains.containsKey(reactiveId) &&
            if (productId != null) model.indexedGrains.containsKey(productId) else true
}

@Serializable
data class Behaviour(
    val name: String,
    val mainReaction: Reaction,
    val description: String = "",
    val probability: Double = 1.0,
    val agePredicate: Predicate<Int> = Predicate(Operator.GreaterThanOrEquals, 0),
    val reaction: List<Reaction> = emptyList()
) {

    /** Is behavior applicable for given [grain], [age] and [neighbours] ? */
    fun applicable(grain: Grain, age: Int, neighbours: Map<Direction, Grain>): Boolean =
        mainReaction.reactiveId == grain.id && agePredicate.check(age) &&
                reaction.fold(true) { a, r -> a && r.allowedDirection.any { (neighbours[it]?.id == r.reactiveId) } }

    /** To used only if behaviour is [applicable], returns the used agents directions to complete behavior. */
    fun usedAgents(neighbours: Map<Direction, Grain>) =
        reaction.map { r -> r.allowedDirection.find { (neighbours[it]?.id == r.reactiveId) } }.filterNotNull()


    fun validForModel(model: Model) = name.isNotBlank() && probability >= 0.0 && probability <= 1.0 &&
            mainReaction.validForModel(model) && reaction.fold(true) { a, r -> a && r.validForModel(model) }
}

@Serializable
data class Position(
    val x: Int, val y: Int, val z: Int = 0
) {
    fun move(direction: Direction, step: Int = 1) = when (direction) {
        Direction.Left -> copy(x = x - step)
        Direction.Right -> copy(x = x + step)
        Direction.Up -> copy(y = y + step)
        Direction.Down -> copy(y = y - step)
        Direction.Front -> copy(z = z - step)
        Direction.Back -> copy(z = z + step)
    }
}

@Serializable
data class Model(
    val id: String,
    val width: Int = 100,
    val height: Int = 100,
    val depth: Int = 1,
    val description: String = "",
    val grains: List<Grain> = emptyList(),
    val behaviours: List<Behaviour> = emptyList()
) {
    val indexedGrains: Map<Int, Grain> = grains.map { it.id to it }.toMap()

    val dataSize = width * height * depth

    fun toIndex(position: Position) = position.z * (height * width) + position.y * width + position.x

    fun toPosition(index: Int): Position {
        val zDelta = height * width
        val z = index / zDelta
        val yRest = index - z * zDelta
        val y = yRest / width
        return Position(yRest - y * width, y, z)
    }

    fun inside(position: Position) = position.z in 0 until depth && position.y in 0 until height && position.x in 0 until width

    val valid
        get() =
            id.isNotBlank() && width > 0 && height > 0 && depth > 0 &&
                    grains.fold(true) { a, g -> a && g.valid } &&
                    behaviours.fold(true) { a, g -> a && g.validForModel(this) }

}

@Serializable
data class Simulation(
    val model: Model,
    val initialAgents: Array<Int> = Array(model.dataSize) { -1 },
    val agents: Array<Int> = initialAgents.copyOf(),
    val ages: Array<Int> = Array(model.dataSize) { -1 }
) {

    fun reset() {
        initialAgents.copyInto(agents)
        for (i in 0 until ages.size) {
            ages[i] = if (agents[i] != -1) 0 else -1
        }
    }

    fun grainAtIndex(index: Int) = model.indexedGrains[agents[index]]

    fun ageAtIndex(index: Int) = ages[index]

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

    fun addGrainAtIndex(index: Int, grain: Grain) {
        agents[index] = grain.id
        ages[index] = 0
    }

    fun removeGrainAtIndex(index: Int) {
        agents[index] = -1
        ages[index] = -1
    }


    fun neighbours(index: Int): Map<Direction, Grain> = model.toPosition(index).let { position ->
        Direction.values().asSequence()
            .map { it to position.move(it) }.filter { model.inside(it.second) }
            .mapNotNull { grainAtIndex(model.toIndex(it.second)).let { grain -> if (grain != null) it.first to grain else null } }
            .toMap()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Simulation

        if (model != other.model) return false
        if (!initialAgents.contentEquals(other.initialAgents)) return false
        if (!agents.contentEquals(other.agents)) return false
        if (!ages.contentEquals(other.ages)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = model.hashCode()
        result = 31 * result + initialAgents.contentHashCode()
        result = 31 * result + agents.contentHashCode()
        result = 31 * result + ages.contentHashCode()
        return result
    }
}


