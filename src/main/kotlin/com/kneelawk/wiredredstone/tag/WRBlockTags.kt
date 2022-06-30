package com.kneelawk.wiredredstone.tag

import com.kneelawk.wiredredstone.WRConstants
import net.minecraft.block.Block
import net.minecraft.tag.TagKey
import net.minecraft.util.registry.Registry

object WRBlockTags {
    val WIRE_FORCE_CONNECTABLE: TagKey<Block> by lazy {
        TagKey.of(Registry.BLOCK_KEY, WRConstants.id("wire_force_connectable"))
    }
    val WIRE_FORCE_NOT_CONNECTABLE: TagKey<Block> by lazy {
        TagKey.of(Registry.BLOCK_KEY, WRConstants.id("wire_force_not_connectable"))
    }
}
