package com.kneelawk.wiredredstone.wirenet

import com.kneelawk.wiredredstone.part.AbstractSidedPart
import com.kneelawk.wiredredstone.util.SidedPos
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

interface SidedPartExtType : PartExtType {
    override fun createExtsForContainer(world: World, pos: BlockPos, part: NetNodeContainer): Set<PartExt> {
        return (part as? AbstractSidedPart)?.let { createExtsForPart(world, SidedPos(pos.toImmutable(), it.side), it) }
            ?: emptySet()
    }

    fun createExtsForPart(world: World, pos: SidedPos, part: AbstractSidedPart): Set<PartExt>
}