package com.kneelawk.wiredredstone.node

import alexiil.mc.lib.multipart.api.AbstractPart
import com.kneelawk.graphlib.api.graph.NodeHolder
import com.kneelawk.graphlib.api.graph.user.BlockNode

interface PartBlockNode {
    fun getPart(self: NodeHolder<BlockNode>): AbstractPart?
}
