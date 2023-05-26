package com.kneelawk.wiredredstone.util

import com.kneelawk.graphlib.api.graph.NodeContext
import com.kneelawk.graphlib.api.node.SidedBlockNode
import com.kneelawk.graphlib.api.util.SidedPos
import com.kneelawk.wiredredstone.part.SidedPart

inline fun <reified T> NodeContext.getSidedPart(): T? {
    val node = self.node as? SidedBlockNode ?: return null
    return SidedPart.getPart(blockWorld, SidedPos(pos, node.side)) as? T
}
