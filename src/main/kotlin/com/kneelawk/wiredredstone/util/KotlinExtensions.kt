package com.kneelawk.wiredredstone.util

import java.util.*

fun <T> T?.requireNotNull(msg: String): T {
    return Objects.requireNonNull(this, msg)!!
}

/**
 * Returns the largest value among all values produced by [selector], constrained to be ([min] <= x <= [max]) function
 * applied to each element in the collection or [min] if there are no elements.
 */
inline fun <T, R : Comparable<R>> Iterable<T>.constrainedMaxOfOrNull(min: R, max: R, selector: (T) -> R): R {
    val iterator = iterator()
    var maxValue = min
    while (iterator.hasNext()) {
        val v = selector(iterator.next())
        if (v >= max) {
            return max
        }
        if (maxValue < v) {
            maxValue = v
        }
    }
    return maxValue
}
