package com.kneelawk.wiredredstone.wirenet

import com.kneelawk.graphlib.graph.BlockNode
import com.kneelawk.wiredredstone.util.RedstoneWireType
import net.minecraft.world.World

interface RedstoneCarrierPartExt : BlockNode {
    val redstoneType: RedstoneWireType

    fun getState(world: World, self: NetNode): Int

    fun setState(world: World, self: NetNode, state: Int)

    fun getInput(world: World, self: NetNode): Int
}