package com.kneelawk.wiredredstone.wirenet

import com.kneelawk.graphlib.graph.BlockNode

interface NetNodeContainer {
    fun createExtsForContainer(): Collection<BlockNode>
}