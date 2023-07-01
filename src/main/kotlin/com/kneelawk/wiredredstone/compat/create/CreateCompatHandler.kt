package com.kneelawk.wiredredstone.compat.create

import com.kneelawk.wiredredstone.WRLog
import com.kneelawk.wiredredstone.util.ReflectionUtils
import net.fabricmc.loader.api.FabricLoader

object CreateCompatHandler {
    private var compat: CreateCompat? = null

    fun init() {
        if (FabricLoader.getInstance().isModLoaded("create")) {
            compat = ReflectionUtils.loadIntegrationObject(
                "com.kneelawk.wiredredstone.compat.create.impl.CreateCompatImpl", "Create"
            )
            try {
                compat?.init()
            } catch (t: Throwable) {
                WRLog.log.error(
                    "Attempted to load Create integration, but encountered an error. Create integration will not work.",
                    t
                )
                compat = null
            }
        }
    }
}
