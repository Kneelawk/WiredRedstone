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
            try {
                integration = ReflectionUtils.loadObject("com.kneelawk.wiredredstone.compat.emi.EMIIntegrationImpl")
            } catch (ex: ClassNotFoundException) {
                WRLog.warn(
                    "Attempted to load EMI integration, but found that this version of Wired Redstone is " +
                            "not compiled with EMI integration. EMI integration will not work."
                )
            }
        }
    }

    @Environment(EnvType.CLIENT)
    fun openRedstoneAssemblerRecipes() {
        try {
            integration?.openRedstoneAssemblerRecipes()
        } catch (t: Throwable) {
            WRLog.log.error("Attempted to use EMI integration, but encountered an error.", t)
        }
    }
}
