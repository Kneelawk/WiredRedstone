package com.kneelawk.wiredredstone.util

import java.util.stream.Stream

fun <T> T?.requireNonNull(msg: String): T {
    if (this == null) throw NullPointerException(msg)
    return this
}

/**
 * Returns the largest value among all values produced by [selector], constrained to be ([min] <= x <= [max]) function
 * applied to each element in the collection or [min] if there are no elements.
 */
inline fun <T, R : Comparable<R>> Iterable<T>.constrainedMaxOf(min: R, max: R, selector: (T) -> R): R {
    return iterator().constrainedMaxOf(min, max, selector)
}

inline fun <T, R : Comparable<R>> Stream<T>.constrainedMaxOf(min: R, max: R, selector: (T) -> R): R {
    return iterator().constrainedMaxOf(min, max, selector)
}

inline fun <T, R : Comparable<R>> Sequence<T>.constrainedMaxOf(min: R, max: R, selector: (T) -> R): R {
    return iterator().constrainedMaxOf(min, max, selector)
}

inline fun <T, R : Comparable<R>> Iterator<T>.constrainedMaxOf(min: R, max: R, selector: (T) -> R): R {
    var maxValue = min
    while (hasNext()) {
        val v = selector(next())
        if (v >= max) {
            return max
        }
        if (maxValue < v) {
            maxValue = v
        }
    }
    return maxValue
}

fun <E : Enum<E>> E.toByte(): Byte {
    return ordinal.toByte()
}

inline fun <reified E : Enum<E>> Byte.toEnum(): E {
    val values = enumValues<E>()
    return values[toInt() % values.size]
}

fun <E : Enum<E>> E.toInt(): Int {
    return ordinal
}

inline fun <reified E : Enum<E>> Int.toEnum(): E {
    val values = enumValues<E>()
    return values[this % values.size]
}

// From https://discuss.kotlinlang.org/t/arrays-from-ranges/5216/3
fun IntProgression.toArray() = IntArray(this.count()).also { forEachIndexed { index, i -> it[index] = i } }

fun <K, V : Any> MutableMap<K, V>.mergeAll(other: Map<K, V>, remappingFunction: (cur: V, V) -> V) {
    other.forEach { (k, v) -> merge(k, v, remappingFunction) }
}
