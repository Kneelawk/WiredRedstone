package com.kneelawk.wiredredstone.wirenet

import net.minecraft.nbt.NbtCompound
import net.minecraft.server.world.ServerWorld
import net.minecraft.world.PersistentState
import net.minecraft.world.dimension.DimensionType

// This is almost completely copied from 2xsaiko's HCTM-Base.

class WireNetworkState(world: ServerWorld) : PersistentState() {
    companion object {
        fun load(tag: NbtCompound, world: ServerWorld): WireNetworkState {
            val state = WireNetworkState(world)
            state.controller = WireNetworkController.fromTag(tag, world, state::markDirty)
            return state
        }

        fun nameFor(dimension: DimensionType) = "wiredredstone_wirenet${dimension.suffix}"
    }

    var controller = WireNetworkController(world, ::markDirty)

    override fun writeNbt(nbt: NbtCompound): NbtCompound {
        return nbt.copyFrom(controller.toTag())
    }
}

fun ServerWorld.getWireNetworkState(): WireNetworkState {
    val dimension = dimension
    return persistentStateManager.getOrCreate(
        { WireNetworkState.load(it, this) },
        { WireNetworkState(this) },
        WireNetworkState.nameFor(dimension)
    )
}
