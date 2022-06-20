package com.kneelawk.wiredredstone.screenhandler

import com.kneelawk.wiredredstone.WRConstants.id
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.util.registry.Registry

object WRScreenHandlers {
    val REDSTONE_ASSEMBLER = ScreenHandlerType(::RedstoneAssemblerScreenHandler)

    fun init() {
        Registry.register(Registry.SCREEN_HANDLER, id("redstone_assembler"), REDSTONE_ASSEMBLER)
    }
}
