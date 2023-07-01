package com.kneelawk.wiredredstone.compat.cc

import com.kneelawk.wiredredstone.WRLog
import com.kneelawk.wiredredstone.util.ReflectionUtils
import net.fabricmc.loader.api.FabricLoader

object CCIntegrationHandler {
    private var integration: CCIntegration? = null

    fun init() {
        if (FabricLoader.getInstance().isModLoaded("computercraft")) {
            integration = ReflectionUtils.loadIntegrationObject(
                "com.kneelawk.wiredredstone.compat.cc.impl.CCIntegrationImpl", "ComputerCraft"
            )
            try {
                integration?.init()
            } catch (t: Throwable) {
                WRLog.log.error("Attempted to load ComputerCraft integration, but encountered an error. ComputerCraft integration will not work.", t)
                integration = null
            }
        }
    }
}
