package com.kneelawk.wiredredstone.compat.emi

import com.kneelawk.wiredredstone.WRLog
import com.kneelawk.wiredredstone.util.ReflectionUtils
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.loader.api.FabricLoader

object EMIIntegrationHandler {
    private var integration: EMIIntegration? = null
    val loaded: Boolean
        get() = integration != null

    fun init() {
        if (FabricLoader.getInstance().isModLoaded("emi")) {
            integration = ReflectionUtils.loadIntegrationObject(
                "com.kneelawk.wiredredstone.compat.emi.impl.EMIIntegrationImpl", "EMI"
            )
        }
    }

    @Environment(EnvType.CLIENT)
    fun openRedstoneAssemblerRecipes() {
        try {
            integration?.openRedstoneAssemblerRecipes()
        } catch (t: Throwable) {
            WRLog.log.error("Encountered an error with EMI integration.", t)
        }
    }
}
