package com.kneelawk.wiredredstone.part

import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction

interface PhantomRedstoneProviderPart {
    fun getStrongRedstonePower(original: Int, world: ServerWorld, pos: BlockPos, oppositeFace: Direction): Int

    fun getWeakRedstonePower(original: Int, world: ServerWorld, pos: BlockPos, oppositeFace: Direction): Int
}
