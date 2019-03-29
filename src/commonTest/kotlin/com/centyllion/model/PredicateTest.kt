package com.centyllion.model

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PredicateTest {

    @Test
    fun testPredicateEquals() {
        val p = Predicate(Operator.Equals, 10)
        for (i in 0 until 100) {
            if (i == 10 ){
                assertTrue(p.check(i), "$p should be true for $i")
            } else {
                assertFalse(p.check(i), "$p should be false for $i")
            }
        }
    }

    @Test
    fun testPredicateGreaterThan() {
        val p = Predicate(Operator.GreaterThan, 25)
        for (i in 0 until 100) {
            if (i > 25 ){
                assertTrue(p.check(i), "$p should be true for $i")
            } else {
                assertFalse(p.check(i), "$p should be false for $i")
            }
        }
    }

    @Test
    fun testPredicateGreaterThanOrEquals() {
        val p = Predicate(Operator.GreaterThanOrEquals, 25)
        for (i in 0 until 100) {
            if (i >= 25 ){
                assertTrue(p.check(i), "$p should be true for $i")
            } else {
                assertFalse(p.check(i), "$p should be false for $i")
            }
        }
    }

    @Test
    fun testPredicateLessThan() {
        val p = Predicate(Operator.LessThan, 25)
        for (i in 0 until 100) {
            if (i < 25 ){
                assertTrue(p.check(i), "$p should be true for $i")
            } else {
                assertFalse(p.check(i), "$p should be false for $i")
            }
        }
    }

    @Test
    fun testPredicateLessThanOrEquals() {
        val p = Predicate(Operator.LessThanOrEquals, 25)
        for (i in 0 until 100) {
            if (i <= 25 ){
                assertTrue(p.check(i), "$p should be true for $i")
            } else {
                assertFalse(p.check(i), "$p should be false for $i")
            }
        }
    }
}
