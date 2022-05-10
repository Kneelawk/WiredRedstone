package com.kneelawk.wiredredstone.util

import com.kneelawk.graphlib.graph.BlockNode
import com.kneelawk.wiredredstone.wirenet.NetNode
import net.minecraft.util.math.BlockPos

val NetNode.pos: BlockPos
    get() = data().pos()

val NetNode.ext: BlockNode
    get() = data().node()
