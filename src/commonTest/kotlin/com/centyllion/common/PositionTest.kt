package com.centyllion.common

import kotlin.test.Test
import kotlin.test.assertEquals


class PositionTest {

    val m1 = Model("m1")
    val m2 = Model("m2", 256, 100)
    val m3 = Model("m3", 50, 20, 3)

    private fun checkPosition(position: Position, vararg tests: Pair<Model, Int>) = tests.forEach {
        assertEquals(it.second, it.first.toIndex(position))
    }

    private fun checkIndex(index: Int, vararg tests: Pair<Model, Position>) = tests.forEach {
        assertEquals(it.second, it.first.toPosition(index))
    }

    private fun checkInside(position: Position, vararg tests: Pair<Model, Boolean>) = tests.forEach {
        assertEquals(it.second, it.first.inside(position))
    }

    @Test
    fun testPositionToIndex() {
        checkPosition(Position(0, 0, 0), m1 to 0, m2 to 0, m3 to 0)
        checkPosition(Position(50, 0, 0), m1 to 50, m2 to 50)
        checkPosition(Position(50, 2, 0), m1 to 250, m2 to 562, m3 to 150)
        checkPosition(Position(50, 2, 1), m3 to 1150)
    }

    @Test
    fun testIndexToPosition() {
        Position(0, 0, 0).let { zero -> checkIndex(0, m1 to zero, m2 to zero, m3 to zero) }
        checkIndex(50,
            m1 to Position(50, 0, 0),
            m2 to Position(50, 0, 0),
            m3 to Position(0, 1, 0))
        checkIndex(250,
            m1 to Position(50, 2, 0),
            m2 to Position(250, 0, 0),
            m3 to Position(0, 5, 0)
        )
        checkIndex(562,
            m1 to Position(62, 5, 0),
            m2 to Position(50, 2, 0),
            m3 to Position(12, 11, 0)
        )
    }

    @Test
    fun testInside() {
        checkInside(Position(0, 0, 0), m1 to true, m2 to true, m3 to true)
        checkInside(Position(50, 0, 0), m1 to true, m2 to true, m3 to false)
        checkInside(Position(49, 2, 0), m1 to true, m2 to true, m3 to true)
        checkInside(Position(49, 2, 1), m1 to false, m2 to false, m3 to true)
    }

}
