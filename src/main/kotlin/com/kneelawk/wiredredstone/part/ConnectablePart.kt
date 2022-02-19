package com.kneelawk.wiredredstone.part

import net.minecraft.util.shape.VoxelShape

// The way connections are specified, it doesn't really make sense for the part not to be sided.
interface ConnectablePart : SidedPart {
    fun updateConnections(connections: UByte)

    fun overrideConnections(connections: UByte): UByte

    fun getConnectionBlockingShape(): VoxelShape
}