package com.kneelawk.wiredredstone.util

import kotlin.reflect.KProperty

class ThreadLocalProperty<T>(initializer: () -> T) {
    val threadLocal: ThreadLocal<T> = ThreadLocal.withInitial(initializer)

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return threadLocal.get()
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        threadLocal.set(value)
    }
}

fun <T> threadLocal(initializer: () -> T) = ThreadLocalProperty(initializer)
