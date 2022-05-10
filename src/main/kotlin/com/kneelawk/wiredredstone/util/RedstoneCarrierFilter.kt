package com.kneelawk.wiredredstone.util

import com.kneelawk.graphlib.graph.BlockNode
import com.kneelawk.graphlib.wire.WireConnectionFilter
import com.kneelawk.wiredredstone.wirenet.RedstoneCarrierPartExt

object RedstoneCarrierFilter : WireConnectionFilter {
    override fun accepts(self: BlockNode, other: BlockNode): Boolean {
        val d1 = self as? RedstoneCarrierPartExt ?: return false
        val d2 = other as? RedstoneCarrierPartExt ?: return false
        return d1.redstoneType.canConnect(d2.redstoneType)
    }
}
