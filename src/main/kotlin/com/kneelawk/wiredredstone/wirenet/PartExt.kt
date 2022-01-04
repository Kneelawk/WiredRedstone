package com.kneelawk.wiredredstone.wirenet

import net.minecraft.nbt.NbtElement
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos

// This is almost completely copied from 2xsaiko's HCTM-Base.

/**
 * This must be immutable and have equals/hashCode implemented correctly.
 * You **can** store data here, but again, it must be immutable, and hashed correctly.
 * Kotlin's data class with only `val`s used should do all this automatically, so use that.
 */
interface PartExt {
    val type: PartExtType

    /**
     * Return the nodes that this node wants to connect to.
     * Will only actually connect if other node also wants to connect to this
     */
    // I feel like there should be a better way to do this than passing `self` as a parameter, but the only other ways I
    // can think of involve abstract classes which have their own drawbacks.
    fun tryConnect(self: NetNode, world: ServerWorld, pos: BlockPos, nv: NodeView): Set<NetNode>

    fun toTag(): NbtElement?

    /**
     * Node created, removed, connected, disconnected
     */
    fun onChanged(self: NetNode, world: ServerWorld, pos: BlockPos) {
    }

    override fun hashCode(): Int

    override fun equals(other: Any?): Boolean
}
