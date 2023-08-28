package com.kneelawk.wiredredstone.block

import com.kneelawk.wiredredstone.WRConstants.id
import com.kneelawk.wiredredstone.item.WRItems
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.MapColor
import net.minecraft.block.enums.NoteBlockInstrument
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry

object WRBlocks {
    val REDSTONE_ASSEMBLER by lazy {
        RedstoneAssemblerBlock(
            FabricBlockSettings.create()
                .mapColor(MapColor.RED)
                .instrument(NoteBlockInstrument.BASEDRUM)
                .requiresTool().strength(3.5f)
                .luminance { if (it[RedstoneAssemblerBlock.LIT] == true) 13 else 0 })
    }

    fun init() {
        Registry.register(Registries.BLOCK, id("redstone_assembler"), REDSTONE_ASSEMBLER)
        Registry.register(
            Registries.ITEM, id("redstone_assembler"),
            BlockItem(REDSTONE_ASSEMBLER, WRItems.WIRED_REDSTONE_ITEM_SETTINGS)
        )
        WRItems.WIRED_REDSTONE_ITEMS.add(ItemStack(REDSTONE_ASSEMBLER))
    }
}
