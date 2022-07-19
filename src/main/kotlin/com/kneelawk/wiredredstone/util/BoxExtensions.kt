package com.kneelawk.wiredredstone.util

import net.minecraft.util.math.Box
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes

fun PixelBox(x1: Double, y1: Double, z1: Double, x2: Double, y2: Double, z2: Double): Box {
    return Box(x1 / 16.0, y1 / 16.0, z1 / 16.0, x2 / 16.0, y2 / 16.0, z2 / 16.0)
}

fun PixelBox(x1: Int, y1: Int, z1: Int, x2: Int, y2: Int, z2: Int): Box {
    return Box(
        x1.toDouble() / 16.0, y1.toDouble() / 16.0, z1.toDouble() / 16.0, x2.toDouble() / 16.0, y2.toDouble() / 16.0,
        z2.toDouble() / 16.0
    )
}

fun Box.vs(): VoxelShape = VoxelShapes.cuboid(this)

infix fun VoxelShape.union(other: VoxelShape): VoxelShape = VoxelShapes.union(this, other)
