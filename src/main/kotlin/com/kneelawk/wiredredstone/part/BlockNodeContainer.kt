package com.kneelawk.wiredredstone.part

import com.kneelawk.graphlib.graph.BlockNode

interface BlockNodeContainer {
    fun createBlockNodes(): Collection<BlockNode>
}