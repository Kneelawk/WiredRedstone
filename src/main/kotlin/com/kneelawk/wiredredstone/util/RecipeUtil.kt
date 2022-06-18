package com.kneelawk.wiredredstone.util

object RecipeUtil {
    fun recipeToSlotIndex(index: Int, recipeWidth: Int, recipeHeight: Int, gridWidth: Int): Int {
        val offsetX = if (recipeWidth == 1) 1 else 0
        val offsetY = if (recipeHeight == 1) 1 else 0

        val x = index % recipeWidth
        val y = index / recipeWidth

        return x + offsetX + (y + offsetY) * gridWidth
    }
}
