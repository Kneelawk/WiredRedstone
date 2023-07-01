package com.kneelawk.wiredredstone.compat.create

import com.kneelawk.wiredredstone.WRLog
import com.kneelawk.wiredredstone.util.ReflectionUtils
import net.fabricmc.loader.api.FabricLoader

object CreateCompatHandler {
    private var compat: CreateCompat? = null

    fun init() {
        if (FabricLoader.getInstance().isModLoaded("create")) {
            try {
                compat = ReflectionUtils.loadObject("com.kneelawk.wiredredstone.compat.create.CreateCompatImpl")
                compat?.init()
            } catch (t: Throwable) {
                WRLog.log.error("Encountered error while loading Create compat. Create compat will not work.", t)
                compat = null
            }
        }
    }
}
