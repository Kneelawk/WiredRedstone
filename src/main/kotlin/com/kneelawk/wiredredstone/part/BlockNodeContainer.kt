package com.kneelawk.wiredredstone.part

import com.kneelawk.graphlib.api.graph.user.BlockNode

interface BlockNodeContainer {
    fun createBlockNodes(): Collection<BlockNode>
}
