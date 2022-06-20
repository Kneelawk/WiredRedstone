package com.kneelawk.wiredredstone.util

import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.util.collection.DefaultedList

object InventoryUtils {
    fun findDelegateSlots(
        inventory: Inventory, patternStart: Int, patternWidth: Int, patternHeight: Int, inputStart: Int,
        inputEnd: Int, usePatternSlots: Boolean, preferExact: Boolean
    ): IntArray? {
        val res = IntArray(patternWidth * patternHeight) { -1 }

        // add copies to the inputSlots list, so we can decrement item counts to make sure we don't use the same item twice
        val inputSlots = DefaultedList.ofSize(inputEnd - inputStart, ItemStack.EMPTY)
        for (i in 0 until (inputEnd - inputStart)) {
            inputSlots[i] = inventory.getStack(i + inputStart).copy()
        }

        // first, search the input for an exact match for each target item
        for (t in 0 until (patternWidth * patternHeight)) {
            val target = inventory.getStack(patternStart + t)

            // empty pattern slots are put directly into the delegate slot
            if (target.isEmpty) {
                res[t] = patternStart + t
                continue
            }

            // do the search
            for (i in 0 until (inputEnd - inputStart)) {
                val stack = inputSlots[i]
                if (!stack.isEmpty && target.isOf(stack.item) && ItemStack.areNbtEqual(target, stack)) {
                    // exact match found
                    stack.decrement(1)
                    res[t] = i + inputStart
                    break
                }
            }
        }

        // test to see if we've found everything
        if (res.indexOf(-1) == -1) {
            return res
        }

        // preferring exact means these slots will get overridden, so don't bother
        if (!(usePatternSlots && preferExact)) {

            // next, search the input for an inexact match for each remaining unmatched target item
            for (t in 0 until (patternWidth * patternHeight)) {
                // ignore already matched slots (this includes empty slots)
                if (res[t] != -1) {
                    continue
                }

                // we know this won't be empty
                val target = inventory.getStack(patternStart + t)

                for (i in 0 until (inputEnd - inputStart)) {
                    val stack = inputSlots[i]
                    if (!stack.isEmpty && target.isOf(stack.item)) {
                        // inexact match found
                        stack.decrement(1)
                        res[t] = i + inputStart
                        break
                    }
                }
            }
        }

        // now, we use original indices if we can
        val hasMissing = res.indexOf(-1) != -1
        if (usePatternSlots && hasMissing) {
            for (t in 0 until (patternWidth * patternHeight)) {
                if (res[t] == -1) {
                    res[t] = t + patternStart
                }
            }
        } else if (hasMissing) {
            // we can't use pattern stacks and no matches were found for some slots
            return null
        }

        return res
    }

    fun takeInputs(delegate: Inventory, width: Int, height: Int, slots: IntArray) {
        for (i in 0 until (width * height)) {
            val stack = delegate.getStack(slots[i])
            val item = stack.item
            stack.decrement(1)

            if (stack.isEmpty) {
                delegate.setStack(slots[i], item.recipeRemainder?.let { ItemStack(it) } ?: ItemStack.EMPTY)
            }
        }

        delegate.markDirty()
    }
}
