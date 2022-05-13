package com.kneelawk.wiredredstone.util

import com.kneelawk.graphlib.graph.BlockNode
import net.minecraft.util.math.BlockPos

val NetNode.pos: BlockPos
    get() = data().pos()

val NetNode.node: BlockNode
    get() = data().node()
