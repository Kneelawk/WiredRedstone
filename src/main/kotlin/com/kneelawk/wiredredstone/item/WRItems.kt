package com.kneelawk.wiredredstone.item

import com.kneelawk.wiredredstone.WRConstants
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.util.registry.Registry

object WRItems {
    val WIRED_REDSTONE_ITEM_GROUP: ItemGroup =
        FabricItemGroupBuilder.build(WRConstants.id("wiredredstone")) { ItemStack(RED_ALLOY_WIRE) }
    val WIRED_REDSTONE_ITEM_SETTINGS = Item.Settings().group(WIRED_REDSTONE_ITEM_GROUP)

    val RED_ALLOY_WIRE by lazy { RedAlloyWireItem(WIRED_REDSTONE_ITEM_SETTINGS) }

    fun init() {
        register(RED_ALLOY_WIRE, "red_alloy_wire")
    }

    private fun register(item: Item, name: String) {
        Registry.register(Registry.ITEM, WRConstants.id(name), item)
    }
}