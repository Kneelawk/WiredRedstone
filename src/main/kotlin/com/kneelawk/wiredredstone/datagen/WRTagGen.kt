package com.kneelawk.wiredredstone.datagen

import com.kneelawk.wiredredstone.WRConstants.id
import com.kneelawk.wiredredstone.item.WRItems
import com.kneelawk.wiredredstone.util.DyeColorUtil
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider.ItemTagProvider
import net.minecraft.item.Item
import net.minecraft.registry.HolderLookup
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.tag.TagKey
import net.minecraft.util.DyeColor
import net.minecraft.util.Identifier
import java.util.concurrent.CompletableFuture

class WRTagGen(output: FabricDataOutput, registries: CompletableFuture<HolderLookup.Provider>) :
    ItemTagProvider(output, registries) {
    companion object {
        val DYE_TAGS = DyeColor.values().associateWith {
            TagKey.of(RegistryKeys.ITEM, Identifier("c", it.getName() + "_dyes"))
        }
        val COLORED_BUNDLED_CABLES: TagKey<Item> = TagKey.of(RegistryKeys.ITEM, id("colored_bundled_cables"))
        val INSULATED_WIRES: TagKey<Item> = TagKey.of(RegistryKeys.ITEM, id("insulated_wires"))
        val COLORED_STANDING_BUNDLED_CABLES: TagKey<Item> =
            TagKey.of(RegistryKeys.ITEM, id("colored_standing_bundled_cables"))
        val STANDING_INSULATED_WIRES: TagKey<Item> = TagKey.of(RegistryKeys.ITEM, id("standing_insulated_wires"))
    }

    override fun configure(arg: HolderLookup.Provider) {
        for ((color, tag) in DYE_TAGS) {
            getOrCreateTagBuilder(tag).add(DyeColorUtil.dye(color))
        }
        getOrCreateTagBuilder(COLORED_BUNDLED_CABLES).let {
            for (color in DyeColor.values()) it.add(WRItems.BUNDLED_CABLES[color]!!)
        }
        getOrCreateTagBuilder(INSULATED_WIRES).let {
            for (color in DyeColor.values()) it.add(WRItems.INSULATED_WIRES[color]!!)
        }
        getOrCreateTagBuilder(COLORED_STANDING_BUNDLED_CABLES).let {
            for (color in DyeColor.values()) it.add(WRItems.STANDING_BUNDLED_CABLES[color]!!)
        }
        getOrCreateTagBuilder(STANDING_INSULATED_WIRES).let {
            for (color in DyeColor.values()) it.add(WRItems.STANDING_INSULATED_WIRES[color]!!)
        }
    }
}
