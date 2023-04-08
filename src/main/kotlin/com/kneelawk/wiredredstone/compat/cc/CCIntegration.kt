package com.kneelawk.wiredredstone.compat.cc

import com.kneelawk.graphlib.api.v1.util.SidedPos
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

interface CCIntegration {
    fun init()

    fun getBundledCableInput(world: ServerWorld, pos: SidedPos): UShort

    fun hasBundledCableOutput(world: World, pos: BlockPos): Boolean
}
