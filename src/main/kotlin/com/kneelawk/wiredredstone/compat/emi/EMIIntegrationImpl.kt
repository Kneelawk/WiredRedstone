package com.kneelawk.wiredredstone.compat.emi

import dev.emi.emi.api.EmiApi
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment

@Suppress("unused")
object EMIIntegrationImpl : EMIIntegration {
    @Environment(EnvType.CLIENT)
    override fun openRedstoneAssemblerRecipes() {
        EmiApi.displayRecipeCategory(WiredRedstonePlugin.CATEGORY)
    }
}
