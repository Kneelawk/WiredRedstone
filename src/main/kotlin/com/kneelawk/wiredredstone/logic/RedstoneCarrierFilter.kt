package com.kneelawk.wiredredstone.logic

import com.kneelawk.graphlib.graph.BlockNode
import com.kneelawk.graphlib.wire.WireConnectionFilter
import com.kneelawk.wiredredstone.node.RedstoneCarrierBlockNode

object RedstoneCarrierFilter : WireConnectionFilter {
    override fun accepts(self: BlockNode, other: BlockNode): Boolean {
        val d1 = self as? RedstoneCarrierBlockNode ?: return false
        val d2 = other as? RedstoneCarrierBlockNode ?: return false
        return d1.redstoneType.canConnect(d2.redstoneType)
    }
}
