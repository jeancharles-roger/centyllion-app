package com.centyllion.model


fun <T> List<List<T>>.allCombinations(): List<List<T>> = combine(emptyList())

internal fun <T> List<List<T>>.combine(prefix: List<T>): List<List<T>> =
    when {
        isEmpty() -> listOf(prefix)
        else -> drop(1).let { tail ->
            first().flatMap {
                tail.combine(prefix + it)
            }
        }
    }


enum class DiffAction { Replaced, Added, Removed }

/** Diff description */
data class Diff<T>(val action: DiffAction, val index: Int, val element: T)

/** Computes differences between [this] and [other]. It returns a list of [Diff] that describes the changes */
internal fun <T> List<T>.diff(other: List<T>): List<Diff<T>> {
    var iSource = 0
    var iOther = 0

    val result = mutableListOf<Diff<T>>()
    while (iSource < size || iOther < other.size) {
        when {
            iSource >= size -> {
                // source is empty
                result.add(Diff(DiffAction.Added, iOther, other[iOther]))
                iOther += 1
            }
            iOther >= other.size -> {
                // other empty
                result.add(Diff(DiffAction.Removed, iSource, this[iSource]))
                iSource += 1
            }
            this[iSource] == other[iOther] -> {
                // nothing changed, next
                iSource += 1
                iOther += 1
            }
            iSource + 1 == size || iOther + 1 == other.size -> {
                // last element for source or iOther
                result.add(Diff(DiffAction.Replaced, iSource, other[iOther]))
                iSource += 1
                iOther += 1
            }
            this[iSource + 1] == other[iOther] -> {
                // next source is current other
                result.add(Diff(DiffAction.Removed, iSource, this[iSource]))
                iSource += 1
            }
            this[iSource] == other[iOther+1] -> {
                // next other is current source
                result.add(Diff(DiffAction.Added, iOther, other[iOther]))
                iOther += 1
            }
            else -> {
                // just different
                result.add(Diff(DiffAction.Replaced, iSource, other[iOther]))
                iSource += 1
                iOther += 1
            }
        }
    }

    return result
}


/** From a list of [Diff] ([diff]) it computes the result one */
internal fun <T> List<T>.applyDiff(diff: List<Diff<T>>): List<T> {
    val result = this.toMutableList()
    var delta = 0
    diff.forEach {
        when (it.action) {
            DiffAction.Added -> {
                result.add(it.index, it.element)
                delta -= 1
            }
            DiffAction.Removed -> {
                result.removeAt(it.index - delta)
                delta += 1
            }
            DiffAction.Replaced -> result[it.index - delta] = it.element
        }
    }
    return result
}
