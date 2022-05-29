package com.kneelawk.wiredredstone.cc

import com.kneelawk.graphlib.util.SidedPos
import com.kneelawk.wiredredstone.util.BundledCableUtils
import dan200.computercraft.api.ComputerCraftAPI
import dan200.computercraft.shared.common.IBundledRedstoneBlock
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

@Suppress("unused")
object CCIntegrationImpl : CCIntegration {
    override fun init() {
        ComputerCraftAPI.registerBundledRedstoneProvider { world, pos, side ->
            val output = BundledCableUtils.getBundledCableOutput(world, SidedPos(pos, side))
                ?: return@registerBundledRedstoneProvider -1
            BundledCableUtils.long2Short(output).toInt()
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
