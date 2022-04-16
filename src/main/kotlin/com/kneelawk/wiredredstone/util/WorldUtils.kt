package com.kneelawk.wiredredstone.util

import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World

object WorldUtils {
    fun strongUpdateNeighbors(world: World, pos: BlockPos, edge: Direction) {
        val state = world.getBlockState(pos)
        val offset = pos.offset(edge)

        world.updateNeighbors(pos, state.block)

        Direction.values()
            .asSequence()
            .filter { it != edge.opposite }
            .map { offset.offset(it) }
            .forEach { world.updateNeighbor(it, state.block, pos) }
    }
}