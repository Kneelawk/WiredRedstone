package com.kneelawk.wiredredstone.part

import com.kneelawk.graphlib.api.v1.node.BlockNode

interface BlockNodeContainer {
    fun createBlockNodes(): Collection<BlockNode>
}
