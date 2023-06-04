package com.kneelawk.wiredredstone.util

import com.kneelawk.graphlib.api.graph.NodeHolder
import com.kneelawk.graphlib.api.graph.user.BlockNode
import com.kneelawk.graphlib.api.graph.user.SidedBlockNode
import com.kneelawk.graphlib.api.util.SidedPos
import com.kneelawk.wiredredstone.part.SidedPart

inline fun <reified T : SidedPart> NodeHolder<BlockNode>.getSidedPart(): T? {
    val node = this.node as? SidedBlockNode ?: return null
    return SidedPart.getPart<T>(blockWorld, SidedPos(pos, node.side))
}
