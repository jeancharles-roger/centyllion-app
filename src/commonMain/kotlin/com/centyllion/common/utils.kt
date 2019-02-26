package com.centyllion.common


fun <T> List<List<T>>.allCombinations(): List<List<T>> = combine(emptyList())

internal fun <T> List<List<T>>.combine(prefix: List<T>): List<List<T>> =
    when {
        isEmpty() -> listOf(prefix)
        else -> drop(1).let { tail ->
            first().flatMap {
                tail.combine(prefix + it)
            } }
    }

