package com.kneelawk.wiredredstone.compat.cc.impl

import com.kneelawk.graphlib.api.util.SidedPos
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

        BundledCableLogic.registerPowerSource { world, pos ->
            val input = ComputerCraftAPI.getBundledRedstoneOutput(world, pos.pos.offset(pos.side), pos.side.opposite)

            if (input == -1) {
                return@registerPowerSource 0u
            }

            return@registerPowerSource BundledCableLogic.digital2Analog(input.toUShort())
        }

        BundledCableLogic.registerConnectionFinder { world, pos ->
            ComputerCraftAPI.getBundledRedstoneOutput(world, pos.pos, pos.side) != -1
        }
    }
}
