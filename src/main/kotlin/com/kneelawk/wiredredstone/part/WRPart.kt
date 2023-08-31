package com.kneelawk.wiredredstone.part

import net.minecraft.util.math.BlockPos
import net.minecraft.util.shape.VoxelShape

interface WRPart {
    fun getPos(): BlockPos

    fun getShape(): VoxelShape
}
