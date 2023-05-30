package com.kneelawk.wiredredstone.util.connectable

import com.kneelawk.graphlib.api.graph.NodeContext
import com.kneelawk.graphlib.api.util.HalfLink
import com.kneelawk.graphlib.api.wire.SidedWireBlockNode
import com.kneelawk.graphlib.api.wire.SidedWireConnectionFilter
import com.kneelawk.graphlib.api.wire.WireConnectionType
import net.minecraft.util.math.Direction

class WireCornerBlockageFilter(
    private val wireSide: Direction, private val wireWidth: Double, private val wireHeight: Double
) : SidedWireConnectionFilter {
    override fun canConnect(
        self: SidedWireBlockNode, ctx: NodeContext, inDirection: Direction, connectionType: WireConnectionType,
        link: HalfLink
    ): Boolean {
        return ConnectableUtils.canWireCornerConnect(
            ctx.blockWorld, ctx.pos, inDirection, connectionType, wireSide, wireWidth, wireHeight
        )
    }
}
