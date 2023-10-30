package com.kneelawk.wiredredstone.util

import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.item.Item
import net.minecraft.item.Items
import net.minecraft.util.DyeColor
import net.minecraft.util.DyeColor.*

object DyeColorUtil {
    private val DYE_COLORS_TO_WOOL = mapOf(
        WHITE to Blocks.WHITE_WOOL,
        ORANGE to Blocks.ORANGE_WOOL,
        MAGENTA to Blocks.MAGENTA_WOOL,
        LIGHT_BLUE to Blocks.LIGHT_BLUE_WOOL,
        YELLOW to Blocks.YELLOW_WOOL,
        LIME to Blocks.LIME_WOOL,
        PINK to Blocks.PINK_WOOL,
        GRAY to Blocks.GRAY_WOOL,
        LIGHT_GRAY to Blocks.LIGHT_GRAY_WOOL,
        CYAN to Blocks.CYAN_WOOL,
        PURPLE to Blocks.PURPLE_WOOL,
        BLUE to Blocks.BLUE_WOOL,
        BROWN to Blocks.BROWN_WOOL,
        GREEN to Blocks.GREEN_WOOL,
        RED to Blocks.RED_WOOL,
        BLACK to Blocks.BLACK_WOOL
    )

    private val DYE_COLORS_TO_ITEM = mapOf(
        WHITE to Items.WHITE_DYE,
        ORANGE to Items.ORANGE_DYE,
        MAGENTA to Items.MAGENTA_DYE,
        LIGHT_BLUE to Items.LIGHT_BLUE_DYE,
        YELLOW to Items.YELLOW_DYE,
        LIME to Items.LIME_DYE,
        PINK to Items.PINK_DYE,
        GRAY to Items.GRAY_DYE,
        LIGHT_GRAY to Items.LIGHT_GRAY_DYE,
        CYAN to Items.CYAN_DYE,
        PURPLE to Items.PURPLE_DYE,
        BLUE to Items.BLUE_DYE,
        BROWN to Items.BROWN_DYE,
        GREEN to Items.GREEN_DYE,
        RED to Items.RED_DYE,
        BLACK to Items.BLACK_DYE
    )

    private val DYE_COLORS_TO_GLASS = mapOf(
        WHITE to Items.WHITE_STAINED_GLASS,
        ORANGE to Items.ORANGE_STAINED_GLASS,
        MAGENTA to Items.MAGENTA_STAINED_GLASS,
        LIGHT_BLUE to Items.LIGHT_BLUE_STAINED_GLASS,
        YELLOW to Items.YELLOW_STAINED_GLASS,
        LIME to Items.LIME_STAINED_GLASS,
        PINK to Items.PINK_STAINED_GLASS,
        GRAY to Items.GRAY_STAINED_GLASS,
        LIGHT_GRAY to Items.LIGHT_GRAY_STAINED_GLASS,
        CYAN to Items.CYAN_STAINED_GLASS,
        PURPLE to Items.PURPLE_STAINED_GLASS,
        BLUE to Items.BLUE_STAINED_GLASS,
        BROWN to Items.BROWN_STAINED_GLASS,
        GREEN to Items.GREEN_STAINED_GLASS,
        RED to Items.RED_STAINED_GLASS,
        BLACK to Items.BLACK_STAINED_GLASS,
    )

    fun wool(color: DyeColor): Block {
        return DYE_COLORS_TO_WOOL[color]!!
    }

    fun dye(color: DyeColor): Item {
        return DYE_COLORS_TO_ITEM[color]!!
    }

    fun glass(color: DyeColor): Item {
        return DYE_COLORS_TO_GLASS[color]!!
    }
}
