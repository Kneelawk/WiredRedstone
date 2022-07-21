package com.kneelawk.wiredredstone.block

import com.kneelawk.wiredredstone.WRConstants.id
import com.kneelawk.wiredredstone.item.WRItems
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.MapColor
import net.minecraft.block.Material
import net.minecraft.item.BlockItem
import net.minecraft.util.registry.Registry

object WRBlocks {
    val REDSTONE_ASSEMBLER by lazy {
        RedstoneAssemblerBlock(
            FabricBlockSettings.of(Material.STONE, MapColor.RED)
                .requiresTool().strength(3.5f)
                .luminance { if (it[RedstoneAssemblerBlock.LIT] == true) 13 else 0 })
    }

    fun init() {
        Registry.register(Registry.BLOCK, id("redstone_assembler"), REDSTONE_ASSEMBLER)
        Registry.register(
            Registry.ITEM, id("redstone_assembler"), BlockItem(REDSTONE_ASSEMBLER, WRItems.WIRED_REDSTONE_ITEM_SETTINGS)
        )
    }
}
