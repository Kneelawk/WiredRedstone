package com.kneelawk.wiredredstone.util

import alexiil.mc.lib.multipart.api.MultipartUtil
import com.kneelawk.graphlib.api.graph.NodeHolder
import com.kneelawk.graphlib.api.graph.user.SidedBlockNode
import com.kneelawk.graphlib.api.util.SidedPos
import com.kneelawk.wiredredstone.WRLog
import com.kneelawk.wiredredstone.part.CenterPart
import com.kneelawk.wiredredstone.part.SidedPart

inline fun <reified T : SidedPart> NodeHolder<*>.getSidedPart(): T? {
    val probablyNode = this.node
    val node = if (probablyNode is SidedBlockNode) {
        probablyNode
    } else {
        WRLog.warn("Tried to get a sided part for a non-sided block node: $probablyNode")
        return null
    }

    return SidedPart.getPart<T>(blockWorld, SidedPos(blockPos, node.side))
}

inline fun <reified T : CenterPart> NodeHolder<*>.getCenterPart(): T? {
    val container = MultipartUtil.get(blockWorld, blockPos) ?: return null
    return container.getFirstPart(T::class.java)
}
