package com.kneelawk.wiredredstone.part

import com.kneelawk.graphlib.api.node.BlockNode

interface BlockNodeContainer {
    fun createBlockNodes(): Collection<BlockNode>
}
