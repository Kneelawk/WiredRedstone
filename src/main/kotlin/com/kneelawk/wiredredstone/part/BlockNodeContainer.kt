package com.kneelawk.wiredredstone.part

import com.kneelawk.graphlib.api.node.BlockNodeDiscovery
import com.kneelawk.graphlib.api.node.KeyBlockNode

interface BlockNodeContainer {
    fun createBlockNodes(): Collection<KeyBlockNode>

    fun discoverBlockNodes(): Collection<BlockNodeDiscovery> {
        return createBlockNodes().map { BlockNodeDiscovery.ofKeyBlockNode(it) }
    }
}
