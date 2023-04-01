package com.kneelawk.wiredredstone.screenhandler

import com.kneelawk.wiredredstone.WRConstants.id
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.resource.featuretoggle.FeatureSet
import net.minecraft.screen.ScreenHandlerType

object WRScreenHandlers {
    val REDSTONE_ASSEMBLER = ScreenHandlerType(::RedstoneAssemblerScreenHandler, FeatureSet.empty())

    fun init() {
        Registry.register(Registries.SCREEN_HANDLER, id("redstone_assembler"), REDSTONE_ASSEMBLER)
    }
}
