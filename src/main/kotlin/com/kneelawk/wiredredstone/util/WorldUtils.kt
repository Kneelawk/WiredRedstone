package com.kneelawk.wiredredstone.util

import alexiil.mc.lib.multipart.api.MultipartUtil
import com.kneelawk.wiredredstone.part.BlockNodeContainer
import net.minecraft.block.Block
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World

object WorldUtils {
    private var updateStack by threadLocal { 0 }
    val doingUpdate
        get() = updateStack > 0

    fun strongUpdateOutputNeighbors(world: World, pos: BlockPos, edge: Direction) {
        val state = world.getBlockState(pos)
        val offset = pos.offset(edge)

        updateNeighbor(world, offset, state.block, pos)

        if (world.getBlockState(offset).isSolidBlock(world, offset)) {
            for (dir in Direction.values()) {
                if (dir != edge.opposite) {
                    updateNeighbor(world, offset.offset(dir), state.block, pos)
                }
            }
        }
    }

    fun strongUpdateAllNeighbors(world: World, pos: BlockPos, edge: Direction) {
        val state = world.getBlockState(pos)
        val offset = pos.offset(edge)

        updateNeighbors(world, pos, state.block)

        if (world.getBlockState(offset).isSolidBlock(world, offset)) {
            for (dir in Direction.values()) {
                if (dir != edge.opposite) {
                    updateNeighbor(world, offset.offset(dir), state.block, pos)
                }
            }
        }
    }

    fun updateNeighbors(world: World, sourcePos: BlockPos, sourceBlock: Block) {
        updateNeighbor(world, sourcePos.west(), sourceBlock, sourcePos)
        updateNeighbor(world, sourcePos.east(), sourceBlock, sourcePos)
        updateNeighbor(world, sourcePos.down(), sourceBlock, sourcePos)
        updateNeighbor(world, sourcePos.up(), sourceBlock, sourcePos)
        updateNeighbor(world, sourcePos.north(), sourceBlock, sourcePos)
        updateNeighbor(world, sourcePos.south(), sourceBlock, sourcePos)
    }

    fun updateNeighbor(world: World, pos: BlockPos, sourceBlock: Block, neighbosPos: BlockPos) {
        val multipart = MultipartUtil.get(world, pos)

        try {
            updateStack++

            if (multipart != null) {
                // TODO: see if this can be optimized out
                if (multipart.getFirstPart { it !is BlockNodeContainer } != null) {
                    world.updateNeighbor(pos, sourceBlock, neighbosPos)
                }
            } else {
                world.updateNeighbor(pos, sourceBlock, neighbosPos)
            }
        } finally {
            updateStack--
        }
    }
}
