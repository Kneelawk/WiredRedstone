package com.kneelawk.wiredredstone.util.connectable

import com.kneelawk.graphlib.wire.SidedWireBlockNode
import com.kneelawk.graphlib.wire.SidedWireConnectionFilter
import com.kneelawk.graphlib.wire.WireConnectionType
import com.kneelawk.wiredredstone.util.NetNode
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction

class WireCornerBlockageFilter(
    private val wireSide: Direction, private val wireWidth: Double, private val wireHeight: Double
) : SidedWireConnectionFilter {
    override fun canConnect(
        self: SidedWireBlockNode, world: ServerWorld, pos: BlockPos, inDirection: Direction,
        connectionType: WireConnectionType, selfNode: NetNode, otherNode: NetNode
    ): Boolean {
        return ConnectableUtils.canWireCornerConnect(
            world, pos, inDirection, connectionType, wireSide, wireWidth, wireHeight
        )
    }
}
