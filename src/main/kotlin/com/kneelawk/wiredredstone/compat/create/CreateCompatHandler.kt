package com.kneelawk.wiredredstone.compat.create

import com.kneelawk.wiredredstone.util.ReflectionUtils
import net.fabricmc.loader.api.FabricLoader

object CreateCompatHandler {
    private var compat: CreateCompat? = null

    fun init() {
        if (FabricLoader.getInstance().isModLoaded("create")) {
            compat = ReflectionUtils.loadObject("com.kneelawk.wiredredstone.compat.create.CreateCompatImpl")
            compat?.init()
        }
    }
}
