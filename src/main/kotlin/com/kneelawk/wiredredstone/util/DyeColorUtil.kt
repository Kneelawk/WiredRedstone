package com.kneelawk.wiredredstone.util

import net.minecraft.block.Block
import net.minecraft.block.Blocks.*
import net.minecraft.util.DyeColor
import net.minecraft.util.DyeColor.*

object DyeColorUtil {
    private val DYE_COLORS_TO_WOOL = mapOf(
        WHITE to WHITE_WOOL,
        ORANGE to ORANGE_WOOL,
        MAGENTA to MAGENTA_WOOL,
        LIGHT_BLUE to LIGHT_BLUE_WOOL,
        YELLOW to YELLOW_WOOL,
        LIME to LIME_WOOL,
        PINK to PINK_WOOL,
        GRAY to GRAY_WOOL,
        LIGHT_GRAY to LIGHT_GRAY_WOOL,
        CYAN to CYAN_WOOL,
        PURPLE to PURPLE_WOOL,
        BLUE to BLUE_WOOL,
        BROWN to BROWN_WOOL,
        GREEN to GREEN_WOOL,
        RED to RED_WOOL,
        BLACK to BLACK_WOOL
    )

    fun wool(color: DyeColor): Block {
        return DYE_COLORS_TO_WOOL[color]!!
    }
}
