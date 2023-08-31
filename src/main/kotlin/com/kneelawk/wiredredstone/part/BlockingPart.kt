package com.kneelawk.wiredredstone.part

import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes

interface BlockingPart : WRPart {
    fun getExternalConnectionBlockingShape(): VoxelShape = getShape()

    fun getInternalConnectionBlockingShape(): VoxelShape = VoxelShapes.empty()
}
