package com.kneelawk.wiredredstone.util

import alexiil.mc.lib.multipart.api.MultipartUtil
import com.kneelawk.graphlib.api.graph.NodeHolder
import com.kneelawk.graphlib.api.graph.user.BlockNode
import com.kneelawk.graphlib.api.graph.user.SidedBlockNode
import com.kneelawk.graphlib.api.util.SidedPos
import com.kneelawk.wiredredstone.part.CenterPart
import com.kneelawk.wiredredstone.part.SidedPart

inline fun <reified T : SidedPart> NodeHolder<BlockNode>.getSidedPart(): T? {
    val node = this.node as? SidedBlockNode ?: return null
    return SidedPart.getPart<T>(blockWorld, SidedPos(blockPos, node.side))
}

inline fun <reified T : CenterPart> NodeHolder<BlockNode>.getCenterPart(): T? {
    val container = MultipartUtil.get(blockWorld, blockPos) ?: return null
    return container.getFirstPart(T::class.java)
}
