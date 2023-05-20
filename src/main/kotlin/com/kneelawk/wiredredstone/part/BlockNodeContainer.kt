package com.kneelawk.wiredredstone.part

import com.kneelawk.graphlib.api.node.BlockNodeDiscovery
import com.kneelawk.graphlib.api.node.UniqueBlockNode

interface BlockNodeContainer {
    fun createBlockNodes(): Collection<UniqueBlockNode>

    fun discoverBlockNodes(): Collection<BlockNodeDiscovery> {
        return createBlockNodes().map { BlockNodeDiscovery.ofUniqueBlockNode(it) }
    }
}
