package com.kneelawk.wiredredstone.screenhandler

import com.kneelawk.wiredredstone.WRConstants.id
import net.minecraft.feature_flags.FeatureFlagBitSet
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.screen.ScreenHandlerType

object WRScreenHandlers {
    val REDSTONE_ASSEMBLER = ScreenHandlerType(::RedstoneAssemblerScreenHandler, FeatureFlagBitSet.empty())

    fun init() {
        Registry.register(Registries.SCREEN_HANDLER_TYPE, id("redstone_assembler"), REDSTONE_ASSEMBLER)
    }
}
