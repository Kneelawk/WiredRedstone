package com.kneelawk.wiredredstone.wirenet

import net.minecraft.nbt.NbtElement
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

interface PartExtType {
    fun createExtFromTag(tag: NbtElement?): PartExt?

    fun createExtsForContainer(world: World, pos: BlockPos, part: NetNodeContainer): Set<PartExt>
}