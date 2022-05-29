package com.kneelawk.wiredredstone.cc

import com.kneelawk.graphlib.util.SidedPos
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

interface CCIntegration {
    fun init()

    fun getBundledCableInput(world: ServerWorld, pos: SidedPos): UShort

    fun hasBundledCableOutput(world: World, pos: BlockPos): Boolean
}
