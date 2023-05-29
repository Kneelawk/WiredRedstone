package com.kneelawk.wiredredstone.node

import com.kneelawk.graphlib.api.graph.user.BlockNode
import com.kneelawk.wiredredstone.logic.RedstoneWireType
import com.kneelawk.wiredredstone.util.NetNode
import net.minecraft.server.world.ServerWorld

interface RedstoneCarrierBlockNode : BlockNode {
    val redstoneType: RedstoneWireType

    fun putPower(world: ServerWorld, self: NetNode, power: Int)

    fun sourcePower(world: ServerWorld, self: NetNode): Int
}
