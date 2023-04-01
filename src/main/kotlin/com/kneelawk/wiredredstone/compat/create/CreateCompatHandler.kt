package com.kneelawk.wiredredstone.compat.create

import com.kneelawk.wiredredstone.WRLog
import com.kneelawk.wiredredstone.util.ReflectionUtils
import net.fabricmc.loader.api.FabricLoader

object CreateCompatHandler {
    private var compat: CreateCompat? = null

    fun init() {
        if (FabricLoader.getInstance().isModLoaded("create")) {
            try {
                compat = ReflectionUtils.loadObject("com.kneelawk.wiredredstone.compat.create.impl.CreateCompatImpl")
            } catch (ex: ClassNotFoundException) {
                WRLog.warn(
                    "Attempted to load Create integration, but found that this version of Wired Redstone is " +
                            "not compiled with Create integration. Create integration will not work."
                )
            }
            compat?.init()
        }
    }
}
