package com.kneelawk.wiredredstone.recipe

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.CraftingInventory
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.recipe.RecipeInputProvider
import net.minecraft.recipe.RecipeMatcher

class DelegatingCraftingInventory(
    private val delegate: Inventory, private val width: Int, private val height: Int, private val offset: Int
) : CraftingInventory(null, 0, 0) {
    override fun getWidth(): Int {
        return width
    }

    override fun getHeight(): Int {
        return height
    }

    override fun size(): Int {
        return width * height
    }

    override fun isEmpty(): Boolean {
        for (i in 0 until (width * height)) {
            if (!delegate.getStack(offset + i).isEmpty) {
                return false
            }
        }

        return true
    }

    override fun getStack(slot: Int): ItemStack {
        return delegate.getStack(offset + slot)
    }

    override fun removeStack(slot: Int): ItemStack {
        return delegate.removeStack(offset + slot)
    }

    override fun removeStack(slot: Int, amount: Int): ItemStack {
        return delegate.removeStack(offset + slot, amount)
    }

    override fun setStack(slot: Int, stack: ItemStack) {
        delegate.setStack(offset + slot, stack)
    }

    override fun markDirty() {
        delegate.markDirty()
    }

    override fun canPlayerUse(player: PlayerEntity): Boolean {
        return delegate.canPlayerUse(player)
    }

    override fun clear() {
        for (i in 0 until (width * height)) {
            delegate.setStack(offset + i, ItemStack.EMPTY)
        }
    }

    override fun provideRecipeInputs(finder: RecipeMatcher) {
        (delegate as? RecipeInputProvider)?.provideRecipeInputs(finder)
    }

    override fun onOpen(player: PlayerEntity) {
        delegate.onOpen(player)
    }

    override fun onClose(player: PlayerEntity) {
        delegate.onClose(player)
    }

    override fun isValid(slot: Int, stack: ItemStack): Boolean {
        return delegate.isValid(offset + slot, stack)
    }
}