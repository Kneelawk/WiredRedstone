package com.kneelawk.wiredredstone.part

import net.minecraft.util.shape.VoxelShape

interface BlockingPart : WRPart {
    fun getConnectionBlockingShape(): VoxelShape = getShape()
}
