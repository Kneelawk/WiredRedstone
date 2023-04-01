package com.kneelawk.wiredredstone.recipe

import com.kneelawk.wiredredstone.block.WRBlocks
import net.minecraft.item.ItemStack
import net.minecraft.recipe.Recipe
import net.minecraft.recipe.RecipeType
import net.minecraft.util.collection.DefaultedList

interface RedstoneAssemblerRecipe : Recipe<RedstoneAssemblerInventory> {
    val energyPerTick: Int
    val cookTime: Int

    override fun getType(): RecipeType<*> = RedstoneAssemblerRecipeType

    fun getViewerOutput(): ItemStack

    override fun getRemainder(inventory: RedstoneAssemblerInventory): DefaultedList<ItemStack> {
        val width = inventory.width
        val height = inventory.height
        val defaultedList = DefaultedList.ofSize(width * height, ItemStack.EMPTY)

        for (x in 0 until width) {
            for (y in 0 until height) {
                val item = inventory.getCraftingStack(x, y).item
                if (item.hasRecipeRemainder()) {
                    defaultedList[x + y * width] = ItemStack(item.recipeRemainder)
                }
            }
        }

        return defaultedList
    }

    override fun createIcon(): ItemStack {
        return ItemStack(WRBlocks.REDSTONE_ASSEMBLER)
    }
}
