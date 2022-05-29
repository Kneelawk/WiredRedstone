package com.kneelawk.wiredredstone.util

object ReflectionUtils {
    inline fun <reified T> loadObject(name: String): T? {
        return Class.forName(name).kotlin.objectInstance as? T
    }
}
