package com.kneelawk.wiredredstone.recipe

import net.minecraft.inventory.SimpleInventory
import net.minecraft.item.ItemStack
import net.minecraft.recipe.RecipeMatcher

class SimpleRedstoneAssemblerInventory(
    size: Int, override val width: Int, override val height: Int, private val craftingOffset: Int
) : SimpleInventory(size), RedstoneAssemblerInventory {
    override fun getCraftingStack(x: Int, y: Int): ItemStack {
        return getStack(craftingOffset + x + y * width)
    }

    override fun provideRecipeInputs(finder: RecipeMatcher) {
        forEachCraftingSlot { finder.addInput(getStack(it)) }
    }

    override fun clearCraftingSlots() {
        forEachCraftingSlot { setStack(it, ItemStack.EMPTY) }
    }

    private inline fun forEachCraftingSlot(f: (Int) -> Unit) {
        for (y in 0 until height) {
            for (x in 0 until width) {
                f(craftingOffset + x + y * width)
            }
        }
    }
}
