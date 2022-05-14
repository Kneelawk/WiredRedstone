package com.kneelawk.wiredredstone.node

import com.kneelawk.graphlib.graph.BlockNode
import com.kneelawk.wiredredstone.util.RedstoneWireType
import com.kneelawk.wiredredstone.util.NetNode
import net.minecraft.world.World

interface RedstoneCarrierBlockNode : BlockNode {
    val redstoneType: RedstoneWireType

    fun getState(world: World, self: NetNode): Int

    fun setState(world: World, self: NetNode, state: Int)

    fun getInput(world: World, self: NetNode): Int
}