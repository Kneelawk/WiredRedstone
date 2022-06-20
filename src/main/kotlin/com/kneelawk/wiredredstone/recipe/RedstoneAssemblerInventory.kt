package com.kneelawk.wiredredstone.recipe

import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.recipe.RecipeInputProvider

interface RedstoneAssemblerInventory : Inventory, RecipeInputProvider {
    val width: Int
    val height: Int

    fun getCraftingStack(x: Int, y: Int): ItemStack

    fun clearCraftingSlots()
}
