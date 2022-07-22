package com.kneelawk.wiredredstone.logic.phantom

import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.minecraft.nbt.NbtElement
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction

interface PhantomRedstoneRef {
    val id: Identifier

    fun toTag(): NbtElement?

    fun getStrongRedstonePower(original: Int, world: ServerWorld, pos: BlockPos, oppositeFace: Direction): Lookup

    fun getWeakRedstonePower(original: Int, world: ServerWorld, pos: BlockPos, oppositeFace: Direction): Lookup

    @Environment(EnvType.CLIENT)
    fun renderProjection(context: WorldRenderContext) {
    }

    sealed interface Lookup {
        val power: Int
        val found: Boolean
    }

    data class Found(override val power: Int) : Lookup {
        override val found: Boolean
            get() = true
    }

    object NotFound : Lookup {
        override val power: Int
            get() = 0
        override val found: Boolean
            get() = false
    }
}
