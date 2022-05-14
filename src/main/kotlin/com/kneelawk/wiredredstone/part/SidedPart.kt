package com.kneelawk.wiredredstone.part

import alexiil.mc.lib.multipart.api.MultipartUtil
import com.kneelawk.graphlib.util.SidedPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockView

interface SidedPart : WRPart {
    companion object {
        fun getPart(world: BlockView, pos: SidedPos): SidedPart? {
            val container = MultipartUtil.get(world, pos.pos) ?: return null
            return container.getFirstPart(SidedPart::class.java)?.getSidedContext()?.getPart(pos.side)
        }
    }

    val side: Direction

    fun getSidedContext(): SidedPartContext

    fun getSidedPos(): SidedPos {
        return SidedPos(getPos().toImmutable(), side)
    }
}