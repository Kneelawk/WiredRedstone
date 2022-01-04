package com.kneelawk.wiredredstone.wirenet

import com.kneelawk.wiredredstone.util.RedstoneWireType
import net.minecraft.world.World

interface RedstoneCarrierPartExt : PartExt {
    val redstoneType: RedstoneWireType

    fun getState(world: World, self: NetNode): Boolean

    fun setState(world: World, self: NetNode, state: Boolean)

    fun getInput(world: World, self: NetNode): Boolean
}