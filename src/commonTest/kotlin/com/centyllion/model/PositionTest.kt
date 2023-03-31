package com.centyllion.model

import kotlin.test.Test
import kotlin.test.assertEquals


class PositionTest {

    val s1 = createSimulation()

    private fun checkPosition(position: Position, vararg tests: Pair<Simulation, Int>) = tests.forEach {
        assertEquals(it.second, it.first.toIndex(position.x, position.y, position.z))
    }

    private fun checkIndex(index: Int, vararg tests: Pair<Simulation, Position>) = tests.forEach {
        assertEquals(it.second, it.first.toPosition(index))
    }

    private fun checkInside(position: Position, vararg tests: Pair<Simulation, Boolean>) = tests.forEach {
        assertEquals(it.second, it.first.positionInside(position))
    }

    @Test
    fun testPositionToIndex() {
        checkPosition(Position(0, 0, 0), s1 to 0)
        checkPosition(Position(50, 0, 0), s1 to 50)
        checkPosition(Position(50, 2, 0), s1 to 250 )
        checkPosition(Position(50, 2, 1))
    }

    @Test
    fun testIndexToPosition() {
        Position(0, 0, 0).let { zero -> checkIndex(0, s1 to zero) }
        checkIndex(50,
            s1 to Position(50, 0, 0),
        )
        checkIndex(250,
            s1 to Position(50, 2, 0),
        )
        checkIndex(562,
            s1 to Position(62, 5, 0),
        )
    }

    @Test
    fun testInside() {
        checkInside(Position(0, 0, 0), s1 to true)
        checkInside(Position(50, 0, 0), s1 to true)
        checkInside(Position(49, 2, 0), s1 to true)
        checkInside(Position(49, 2, 1), s1 to false)
    }

    @Test
    fun testMove() {
        assertEquals(209, s1.moveIndex(210, Direction.Left))
        assertEquals(211, s1.moveIndex(210, Direction.Right))
        assertEquals(110, s1.moveIndex(210, Direction.Up))
        assertEquals(310, s1.moveIndex(210, Direction.Down))
    }

    @Test
    fun testMoveTorus() {
        assertEquals(0, s1.moveIndex(99, Direction.Right))
        assertEquals(99, s1.moveIndex(0, Direction.Left))
        assertEquals(10, s1.moveIndex(9910, Direction.Down))
        assertEquals(9910, s1.moveIndex(10, Direction.Up))
    }

}
