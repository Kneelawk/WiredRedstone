package com.kneelawk.wiredredstone.compat.cc.impl

import com.kneelawk.graphlib.util.SidedPos
import com.kneelawk.wiredredstone.WRLog
import com.kneelawk.wiredredstone.compat.cc.CCIntegration
import com.kneelawk.wiredredstone.logic.BundledCableLogic
import dan200.computercraft.api.ComputerCraftAPI
import dan200.computercraft.shared.common.IBundledRedstoneBlock
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

@Suppress("unused")
object CCIntegrationImpl : CCIntegration {
    override fun init() {
        WRLog.log.info("[Wired Redstone] Enabling Computer Craft integration!")

        ComputerCraftAPI.registerBundledRedstoneProvider { world, pos, side ->
            val output = BundledCableLogic.getBundledCableOutput(world, SidedPos(pos, side))
                ?: return@registerBundledRedstoneProvider -1
            BundledCableLogic.long2Short(output).toInt()
        }
    }

    override fun getBundledCableInput(world: ServerWorld, pos: SidedPos): UShort {
        val input = ComputerCraftAPI.getBundledRedstoneOutput(world, pos.pos.offset(pos.side), pos.side.opposite)

        if (input == -1) {
            return 0u
        }

        return input.toUShort()
    }

    override fun hasBundledCableOutput(world: World, pos: BlockPos): Boolean {
        val state = world.getBlockState(pos)
        return state.block is IBundledRedstoneBlock
    }
}
