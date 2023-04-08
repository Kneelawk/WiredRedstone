package com.kneelawk.wiredredstone.node

import com.kneelawk.graphlib.api.v1.node.BlockNode
import com.kneelawk.wiredredstone.logic.RedstoneWireType
import com.kneelawk.wiredredstone.util.NetNode
import net.minecraft.server.world.ServerWorld

interface RedstoneCarrierBlockNode : BlockNode {
    val redstoneType: RedstoneWireType

    fun getState(world: ServerWorld, self: NetNode): Int

    fun setState(world: ServerWorld, self: NetNode, state: Int)

    fun getInput(world: ServerWorld, self: NetNode): Int
}
