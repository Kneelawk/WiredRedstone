package com.kneelawk.wiredredstone.part

import alexiil.mc.lib.multipart.api.MultipartUtil
import com.kneelawk.wiredredstone.util.SidedPos
import net.minecraft.world.BlockView

interface SidedPart {
    companion object {
        fun getPart(world: BlockView, pos: SidedPos): SidedPart? {
            val container = MultipartUtil.get(world, pos.pos) ?: return null
            return container.getFirstPart(SidedPart::class.java)?.getSidedContext()?.getPart(pos.side)
        }
    }

    fun getSidedContext(): SidedPartContext
}