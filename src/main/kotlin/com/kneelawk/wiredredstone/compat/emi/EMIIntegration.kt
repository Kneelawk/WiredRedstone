package com.kneelawk.wiredredstone.compat.emi

import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment

interface EMIIntegration {
    @Environment(EnvType.CLIENT)
    fun openRedstoneAssemblerRecipes()
}
