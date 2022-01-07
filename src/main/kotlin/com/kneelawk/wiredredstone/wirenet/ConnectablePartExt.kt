package com.kneelawk.wiredredstone.wirenet

import com.kneelawk.wiredredstone.util.ConnectionType
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockView

interface ConnectablePartExt : SidedPartExt {
    fun canConnectAt(world: BlockView, pos: BlockPos, inDirection: Direction, type: ConnectionType): Boolean = true
}