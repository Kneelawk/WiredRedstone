package com.kneelawk.wiredredstone.compat.cc

import com.kneelawk.wiredredstone.WRLog
import com.kneelawk.wiredredstone.util.ReflectionUtils
import net.fabricmc.loader.api.FabricLoader

object CCIntegrationHandler {
    private var integration: CCIntegration? = null

    fun init() {
        if (FabricLoader.getInstance().isModLoaded("computercraft")) {
            try {
                integration =
                    ReflectionUtils.loadObject<CCIntegration>(
                        "com.kneelawk.wiredredstone.compat.cc.impl.CCIntegrationImpl"
                    )
            } catch (ex: ClassNotFoundException) {
                WRLog.warn(
                    "Attempted to load ComputerCraft integration, but found that this version of Wired Redstone is " +
                            "not compiled with ComputerCraft integration. ComputerCraft integration will not work."
                )
            }
            integration?.init()
        }
    }
}
