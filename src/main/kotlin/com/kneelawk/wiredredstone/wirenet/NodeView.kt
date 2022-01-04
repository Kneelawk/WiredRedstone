package com.kneelawk.wiredredstone.wirenet

import com.kneelawk.wiredredstone.util.SidedPos
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos

// This is almost completely copied from 2xsaiko's HCTM-Base.

class NodeView(world: ServerWorld) {
    private val wns = world.getWireNetworkState()

    fun getNodes(pos: SidedPos): Set<NetNode> = wns.controller.getNodesAt(pos)

    fun getNodes(pos: BlockPos): Set<NetNode> = wns.controller.getNodesAt(pos)
}
