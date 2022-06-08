package com.kneelawk.wiredredstone.rei

import com.kneelawk.wiredredstone.WRConstants.id
import me.shedaniel.rei.api.common.category.CategoryIdentifier

object WiredRedstoneREI {
    val REDSTONE_ASSEMBLER_CATEGORY: CategoryIdentifier<RedstoneAssemblerDisplay> =
        CategoryIdentifier.of(id("redstone_assembler"))

    fun recipeToSlotIndex(index: Int, recipeWidth: Int, recipeHeight: Int, gridWidth: Int): Int {
        val offsetX = if (recipeWidth == 1) 1 else 0
        val offsetY = if (recipeHeight == 1) 1 else 0

        val x = index % recipeWidth
        val y = index / recipeWidth

        return x + offsetX + (y + offsetY) * gridWidth
    }
}
