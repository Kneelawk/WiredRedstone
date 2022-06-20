package com.kneelawk.wiredredstone.recipe

import com.kneelawk.wiredredstone.util.InventoryUtils
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.recipe.RecipeInputProvider
import net.minecraft.recipe.RecipeMatcher

class DelegateSlotRedstoneAssemblerInventory(
    private val delegate: Inventory, override val width: Int, override val height: Int, private val slots: IntArray
) : RedstoneAssemblerInventory, InputTaker {
    companion object {
        fun fromPatternAndInput(
            inventory: Inventory, patternStart: Int, patternWidth: Int, patternHeight: Int, inputStart: Int,
            inputEnd: Int, usePatternSlots: Boolean, preferExact: Boolean
        ): DelegateSlotRedstoneAssemblerInventory? {
            return InventoryUtils.findDelegateSlots(
                inventory, patternStart, patternWidth, patternHeight, inputStart, inputEnd, usePatternSlots, preferExact
            )?.let { DelegateSlotRedstoneAssemblerInventory(inventory, patternWidth, patternHeight, it) }
        }
    }

    override fun getCraftingStack(x: Int, y: Int): ItemStack = delegate.getStack(slots[x + y * width])

    override fun clearCraftingSlots() = clear()

    override fun clear() {
        for (i in 0 until (width * height)) {
            delegate.setStack(slots[i], ItemStack.EMPTY)
        }
    }

    override fun size(): Int = width * height

    override fun isEmpty(): Boolean {
        for (i in 0 until (width * height)) {
            if (!delegate.getStack(slots[i]).isEmpty) {
                return false
            }
        }

        return true
    }

    override fun getStack(slot: Int): ItemStack = delegate.getStack(slots[slot])

    override fun removeStack(slot: Int): ItemStack = delegate.removeStack(slots[slot])

    override fun removeStack(slot: Int, amount: Int): ItemStack = delegate.removeStack(slots[slot], amount)

    override fun setStack(slot: Int, stack: ItemStack) = delegate.setStack(slots[slot], stack)

    override fun markDirty() = delegate.markDirty()

    override fun canPlayerUse(player: PlayerEntity): Boolean = delegate.canPlayerUse(player)

    override fun provideRecipeInputs(finder: RecipeMatcher) {
        (delegate as? RecipeInputProvider)?.provideRecipeInputs(finder)
    }

    override fun onOpen(player: PlayerEntity) = delegate.onOpen(player)

    override fun onClose(player: PlayerEntity) = delegate.onClose(player)

    override fun isValid(slot: Int, stack: ItemStack): Boolean = delegate.isValid(slots[slot], stack)

    override fun takeInputs() = InventoryUtils.takeInputs(delegate, width, height, slots)
}
