package com.kneelawk.wiredredstone.compat.emi

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
            integration = ReflectionUtils.loadObject("com.kneelawk.wiredredstone.compat.emi.EMIIntegrationImpl")
        }
    }

    @Environment(EnvType.CLIENT)
    fun openRedstoneAssemblerRecipes() {
        integration?.openRedstoneAssemblerRecipes()
    }
}
