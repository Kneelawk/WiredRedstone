package com.kneelawk.wiredredstone.wirenet

import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockView

interface WirePartExt : SidedPartExt {
    fun canConnectAt(world: BlockView, pos: BlockPos, inDirection: Direction): Boolean = true
}