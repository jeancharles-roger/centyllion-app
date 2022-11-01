package com.centyllion.model

enum class Direction {
    Left, Right, Up, Down, LeftUp, RightUp, LeftDown, RightDown;

    val deltaX get() = when (this) {
        Left, LeftUp, LeftDown -> -1
        Right, RightUp, RightDown -> 1
        else -> 0
    }

    val deltaY get() = when (this) {
        Up, LeftUp, RightUp -> -1
        Down, LeftDown, RightDown -> 1
        else -> 0
    }

    val opposite get() = when (this) {
        Left -> Right
        Right -> Left
        Up -> Down
        Down -> Up
        LeftUp -> RightDown
        RightUp -> LeftDown
        LeftDown -> RightUp
        RightDown -> LeftUp
    }

    companion object {
        val default = setOf(Left, Up, Right, Down)
        val first = setOf(Left, Up, Right, Down)
        val extended = setOf(LeftUp, LeftDown, RightUp, RightDown)
    }
}

