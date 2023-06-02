package com.kneelawk.wiredredstone.part

import alexiil.mc.lib.multipart.api.MultipartUtil
import com.kneelawk.graphlib.api.util.SidedPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockView

interface SidedPart : WRPart {
    companion object {
        inline fun <reified T : SidedPart> getPart(world: BlockView, pos: SidedPos): T? {
            return getPart(world, pos, T::class.java)
        }

        fun <T : SidedPart> getPart(world: BlockView, pos: SidedPos, partClass: Class<T>): T? {
            val container = MultipartUtil.get(world, pos.pos) ?: return null
            return container.getFirstPart(partClass) { it.side == pos.side }
        }
    }

    val side: Direction

    fun getSidedPos(): SidedPos {
        return SidedPos(getPos().toImmutable(), side)
    }
}
