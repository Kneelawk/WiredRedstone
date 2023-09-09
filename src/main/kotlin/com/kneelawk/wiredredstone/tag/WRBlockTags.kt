package com.kneelawk.wiredredstone.tag

import com.kneelawk.wiredredstone.WRConstants
import net.minecraft.block.Block
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.tag.TagKey

object WRBlockTags {
    val WIRE_FORCE_CONNECTABLE: TagKey<Block> by lazy {
        TagKey.of(RegistryKeys.BLOCK, WRConstants.id("wire_force_connectable"))
    }
    val WIRE_FORCE_NOT_CONNECTABLE: TagKey<Block> by lazy {
        TagKey.of(RegistryKeys.BLOCK, WRConstants.id("wire_force_not_connectable"))
    }
    val WIRE_GATE_CONNECTABLE: TagKey<Block> by lazy {
        TagKey.of(RegistryKeys.BLOCK, WRConstants.id("wire_gate_connectable"))
    }
}
