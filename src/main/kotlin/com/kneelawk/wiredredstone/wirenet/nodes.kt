package com.kneelawk.wiredredstone.wirenet

import com.kneelawk.graphlib.graph.BlockNode
import com.kneelawk.graphlib.graph.BlockNodeWrapper
import com.kneelawk.graphlib.graph.struct.Node

typealias NetNode = Node<BlockNodeWrapper<out BlockNode>>
