package com.kneelawk.wiredredstone.compat.cc.impl

import com.kneelawk.graphlib.util.SidedPos
import com.kneelawk.wiredredstone.WRLog
import com.kneelawk.wiredredstone.compat.cc.CCIntegration
import com.kneelawk.wiredredstone.logic.BundledCableLogic
import dan200.computercraft.api.ComputerCraftAPI

@Suppress("unused")
object CCIntegrationImpl : CCIntegration {
    override fun init() {
        WRLog.log.info("[Wired Redstone] Enabling Computer Craft integration!")

        ComputerCraftAPI.registerBundledRedstoneProvider { world, pos, side ->
            val output = BundledCableLogic.getBundledCableOutput(world, SidedPos(pos, side))
                ?: return@registerBundledRedstoneProvider -1
            BundledCableLogic.analog2Digital(output).toInt()
        }

        BundledCableLogic.registerBundledPowerSource { world, pos ->
            val input = ComputerCraftAPI.getBundledRedstoneOutput(world, pos.pos.offset(pos.side), pos.side.opposite)

            if (input == -1) {
                return@registerBundledPowerSource 0u
            }

            return@registerBundledPowerSource BundledCableLogic.digital2Analog(input.toUShort())
        }
    }
}
