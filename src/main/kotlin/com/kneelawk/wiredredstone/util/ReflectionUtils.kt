package com.kneelawk.wiredredstone.util

import com.kneelawk.wiredredstone.WRLog

object ReflectionUtils {
    inline fun <reified T> loadObject(name: String): T? {
        return Class.forName(name).kotlin.objectInstance as? T
    }

    inline fun <reified T> loadIntegrationObject(className: String, modDisplayName: String): T? {
        return try {
            loadObject(className)
        } catch (e: ClassNotFoundException) {
            WRLog.log.warn(
                "[Wired Redstone] Attempted to load {} integration, but Wired Redstone was compiled with {} integration disabled. Wired Redstone <-> {} integration will not work.",
                modDisplayName, modDisplayName, modDisplayName
            )
            null
        }
    }
}
