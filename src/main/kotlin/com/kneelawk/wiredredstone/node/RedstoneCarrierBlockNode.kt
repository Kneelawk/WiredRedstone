package com.kneelawk.wiredredstone.node

import com.kneelawk.graphlib.api.graph.user.BlockNode
import com.kneelawk.wiredredstone.logic.RedstoneWireType
import com.kneelawk.wiredredstone.util.RedstoneNode
import net.minecraft.server.world.ServerWorld

interface RedstoneCarrierBlockNode : BlockNode {
    val redstoneType: RedstoneWireType

    fun putPower(world: ServerWorld, self: RedstoneNode, power: Int)

    fun sourcePower(world: ServerWorld, self: RedstoneNode): Int
}
