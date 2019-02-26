package com.centyllion.common

import kotlin.test.Test
import kotlin.test.assertEquals


class CombinationTest {

    @Test
    fun testCombination1() = assertEquals(
        listOf(listOf("a", "c"), listOf("a", "d")),
        listOf(listOf("a"), listOf("c", "d")).allCombinations()
    )

    @Test
    fun testCombination2() = assertEquals(
        listOf(listOf("a", "c"), listOf("a", "d"), listOf("b", "c"), listOf("b", "d")),
        listOf(listOf("a", "b"), listOf("c", "d")).allCombinations()
    )

    @Test
    fun testCombination3() = assertEquals(
        listOf(
            listOf("a", "c", "e"), listOf("a", "c", "f"),
            listOf("a", "d", "e"), listOf("a", "d", "f"),
            listOf("b", "c", "e"), listOf("b", "c", "f"),
            listOf("b", "d", "e"), listOf("b", "d", "f")
        ),
        listOf(listOf("a", "b"), listOf("c", "d"), listOf("e", "f")).allCombinations()
    )

}
