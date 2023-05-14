package com.kneelawk.wiredredstone.compat.cc

import com.kneelawk.wiredredstone.util.ReflectionUtils
import net.fabricmc.loader.api.FabricLoader

object CCIntegrationHandler {
    private var integration: CCIntegration? = null

    fun init() {
        if (FabricLoader.getInstance().isModLoaded("computercraft")) {
            integration = ReflectionUtils.loadIntegrationObject(
                "com.kneelawk.wiredredstone.compat.cc.impl.CCIntegrationImpl", "ComputerCraft"
            )
            integration?.init()
        }
    }
}
