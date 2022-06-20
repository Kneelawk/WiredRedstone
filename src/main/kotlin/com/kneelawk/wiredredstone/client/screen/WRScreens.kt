package com.kneelawk.wiredredstone.client.screen

import com.kneelawk.wiredredstone.screenhandler.WRScreenHandlers
import net.minecraft.client.gui.screen.ingame.HandledScreens

object WRScreens {
    fun init() {
        HandledScreens.register(WRScreenHandlers.REDSTONE_ASSEMBLER, ::RedstoneAssemblerScreen)
    }
}
