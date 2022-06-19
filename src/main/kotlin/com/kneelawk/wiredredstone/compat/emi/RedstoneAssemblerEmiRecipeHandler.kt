package com.kneelawk.wiredredstone.compat.emi

import com.kneelawk.wiredredstone.blockentity.RedstoneAssemblerBlockEntity
import com.kneelawk.wiredredstone.blockentity.RedstoneAssemblerBlockEntity.Companion.CRAFTING_PATTERN_WIDTH
import com.kneelawk.wiredredstone.blockentity.RedstoneAssemblerBlockEntity.Companion.CRAFTING_SLOT_COUNT
import com.kneelawk.wiredredstone.blockentity.RedstoneAssemblerBlockEntity.Companion.CRAFTING_START_SLOT
import com.kneelawk.wiredredstone.blockentity.RedstoneAssemblerBlockEntity.Companion.CRAFTING_STOP_SLOT
import com.kneelawk.wiredredstone.blockentity.RedstoneAssemblerBlockEntity.Companion.INPUT_START_SLOT
import com.kneelawk.wiredredstone.blockentity.RedstoneAssemblerBlockEntity.Companion.INPUT_STOP_SLOT
import com.kneelawk.wiredredstone.blockentity.RedstoneAssemblerBlockEntity.Companion.OUTPUT_START_SLOT
import com.kneelawk.wiredredstone.blockentity.RedstoneAssemblerBlockEntity.Companion.OUTPUT_STOP_SLOT
import com.kneelawk.wiredredstone.blockentity.RedstoneAssemblerBlockEntity.Companion.SLOT_COUNT
import com.kneelawk.wiredredstone.screenhandler.RedstoneAssemblerScreenHandler
import com.kneelawk.wiredredstone.util.RecipeUtil
import dev.emi.emi.api.EmiRecipeHandler
import dev.emi.emi.api.recipe.EmiRecipe
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.item.ItemStack
import net.minecraft.screen.slot.Slot

object RedstoneAssemblerEmiRecipeHandler : EmiRecipeHandler<RedstoneAssemblerScreenHandler> {
    override fun getInputSources(handler: RedstoneAssemblerScreenHandler): List<Slot> {
        val slots = mutableListOf<Slot>()
        for (i in CRAFTING_START_SLOT until CRAFTING_STOP_SLOT) {
            slots += handler.getSlot(i)
        }
        for (i in OUTPUT_START_SLOT until OUTPUT_STOP_SLOT) {
            slots += handler.getSlot(i)
        }
        for (i in INPUT_START_SLOT until INPUT_STOP_SLOT) {
            slots += handler.getSlot(i)
        }
        for (i in SLOT_COUNT until SLOT_COUNT + 36) {
            slots += handler.getSlot(i)
        }
        return slots
    }

    override fun getCraftingSlots(handler: RedstoneAssemblerScreenHandler): List<Slot> {
        val slots = mutableListOf<Slot>()
        for (i in CRAFTING_START_SLOT until CRAFTING_STOP_SLOT) {
            slots += handler.getSlot(i)
        }
        return slots
    }

    override fun supportsRecipe(recipe: EmiRecipe): Boolean {
        return recipe is RedstoneAssemblerEmiRecipe || (recipe.category == VanillaEmiRecipeCategories.CRAFTING && recipe.supportsRecipeTree())
    }

    override fun mutateFill(
        recipe: EmiRecipe, screen: HandledScreen<RedstoneAssemblerScreenHandler>, stacks: List<ItemStack>
    ): List<ItemStack>? {
        return if (recipe is RedstoneAssemblerEmiRecipe) {
            // set the redstone assembler to assembler mode
            screen.screenHandler.mode = RedstoneAssemblerBlockEntity.Mode.ASSEMBLER

            val slotStacks = mutableListOf<ItemStack>()

            for (i in 0 until CRAFTING_SLOT_COUNT) {
                slotStacks += ItemStack.EMPTY
            }

            for (i in stacks.indices) {
                val slot = RecipeUtil.recipeToSlotIndex(i, recipe.width, recipe.height, CRAFTING_PATTERN_WIDTH)
                slotStacks[slot] = stacks[i]
            }

            slotStacks
        } else if (recipe.category == VanillaEmiRecipeCategories.CRAFTING && recipe.supportsRecipeTree()) {
            // set the redstone assembler to crafting mode
            screen.screenHandler.mode = RedstoneAssemblerBlockEntity.Mode.CRAFTING_TABLE

            stacks
        } else {
            null
        }
    }
}
