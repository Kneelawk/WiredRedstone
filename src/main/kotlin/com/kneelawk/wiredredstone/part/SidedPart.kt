package com.kneelawk.wiredredstone.part

import alexiil.mc.lib.multipart.api.MultipartUtil
import com.kneelawk.graphlib.util.SidedPos
import com.kneelawk.wiredredstone.WRLog
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockView
import net.minecraft.world.World

interface SidedPart : WRPart {
    companion object {
        fun getPart(world: BlockView, pos: SidedPos): SidedPart? {
            if (world is World && world.isClient) {
                WRLog.log.warn("Something attempted to get a sided part on the client. This is not reliable and should not be attempted.", RuntimeException("Stack Trace"))
                return null
            }

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
