package com.centyllion.common

enum class Direction {
    Left, Up, Right, Down, Front, Back
}

val defaultDirection = setOf(Direction.Left, Direction.Up, Direction.Right, Direction.Down)

enum class Figure {
    Square, Triangle, Disk, Diamond, Star
}

enum class Operator {
    Equals, NotEquals, LessThan, LessThanOrEquals, GreaterThan, GreaterThanOrEquals
}

data class Predicate(
    val op: Operator,
    val constant: Double
) {
    fun check(value: Double) = when (op) {
        Operator.Equals -> value == constant
        Operator.NotEquals -> value != constant
        Operator.LessThan -> value < constant
        Operator.LessThanOrEquals -> value <= constant
        Operator.GreaterThan -> value > constant
        Operator.GreaterThanOrEquals -> value >= constant
    }
}

data class Grain(
    val id: Int,
    val name: String,
    val color: String,
    val figure: Figure = Figure.Square,
    val description: String = "",
    val halfLife: Double = 0.0,
    val movementProbability: Double = 1.0,
    val allowedDirection: Set<Direction> = defaultDirection

)

data class Reaction(
    val reactiveName: String,
    val productName: String?,
    val allowedDirection: Set<Direction> = defaultDirection
)

data class Behaviour(
    val name: String,
    val description: String = "",
    val probability: Double = 1.0,
    val agePredicate: Predicate = Predicate(Operator.GreaterThanOrEquals, 0.0),
    val reaction: List<Reaction> = emptyList()
)

data class Model(
    val id: String,
    val description: String = "",
    val width: Int = 100,
    val height: Int = 100,
    val depth: Int = 1,
    val grains: List<Grain> = emptyList(),
    val behaviours: List<Behaviour> = emptyList()

)
