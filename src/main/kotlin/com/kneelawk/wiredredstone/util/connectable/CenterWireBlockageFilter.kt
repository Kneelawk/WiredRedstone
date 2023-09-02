package com.kneelawk.wiredredstone.util.connectable

import com.kneelawk.graphlib.api.graph.NodeHolder
import com.kneelawk.graphlib.api.graph.user.BlockNode
import com.kneelawk.graphlib.api.util.HalfLink
import com.kneelawk.graphlib.api.wire.CenterWireBlockNode
import com.kneelawk.graphlib.api.wire.CenterWireConnectionFilter
import net.minecraft.util.math.Direction

class CenterWireBlockageFilter(private val wireDiameter: Double) : CenterWireConnectionFilter {
    override fun canConnect(
        self: CenterWireBlockNode, holder: NodeHolder<BlockNode>, onSide: Direction, link: HalfLink
    ): Boolean {
        return ConnectableUtils.canCenterWireConnect(holder.blockWorld, holder.blockPos, onSide, link, wireDiameter)
    }
}
