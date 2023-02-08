package com.centyllion.model

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
fun <T>Boolean.orNull(block: () -> T): T? {
    contract { callsInPlace(block, InvocationKind.AT_MOST_ONCE) }
    return if (this) block() else null
}

fun <T> List<List<T>>.allCombinations(): List<List<T>> {
    var count = 1
    for (element in this) count *= element.size
    val result = ArrayList<List<T>>(count)
    combine(emptyList(), result)
    return result
}

internal fun <T> List<List<T>>.combine(prefix: List<T>, result: ArrayList<List<T>>) {
    when {
        isEmpty() -> result.add(prefix)
        else -> {
            val tail: List<List<T>> = drop(1)
            first().forEach { tail.combine(prefix + it, result) }
        }
    }
}

/** Creates a new id excluding [existing] */
fun availableId(existing: Collection<Int>) = existing.toSet().let {
    for (i in existing.indices) {
        if (!it.contains(i)) return i
    }
    existing.size
}

/** Creates a new name excluding [existing] */
fun availableName(existing: Collection<String>, prefix: String): String = existing.toSet().let {
    if (!it.contains(prefix)) return prefix
    for (i in 1..existing.size) {
        "$prefix $i".let { name -> if (!it.contains(name)) return name }
    }
    return "$prefix ${existing.size + 1}"
}

/** Find a color excluding already [used] */
fun availableColor(used: Collection<String>) =
    (colorNames.keys - used).let { if (it.isEmpty()) "red" else it.random() }

fun Float.flatten(threshold: Float, line: Float = 0f) = if (this <= threshold) line else this
