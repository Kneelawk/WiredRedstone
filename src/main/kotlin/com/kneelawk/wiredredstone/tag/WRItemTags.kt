package com.kneelawk.wiredredstone.tag

import com.kneelawk.wiredredstone.WRConstants.id
import net.minecraft.item.Item
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.tag.TagKey

object WRItemTags {
    val SCREW_DRIVERS: TagKey<Item> by lazy {
        TagKey.of(RegistryKeys.ITEM, id("screwdrivers"))
    }
}
