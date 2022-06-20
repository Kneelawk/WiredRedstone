package com.kneelawk.wiredredstone.compat.emi

import com.kneelawk.wiredredstone.WRConstants
import com.kneelawk.wiredredstone.WRLog
import com.kneelawk.wiredredstone.block.WRBlocks
import com.kneelawk.wiredredstone.recipe.RedstoneAssemblerRecipeType
import com.kneelawk.wiredredstone.screenhandler.WRScreenHandlers
import dev.emi.emi.api.EmiPlugin
import dev.emi.emi.api.EmiRegistry
import dev.emi.emi.api.recipe.EmiRecipeCategory
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories
import dev.emi.emi.api.render.EmiTexture
import dev.emi.emi.api.stack.EmiStack

@Suppress("unused")
class WiredRedstonePlugin : EmiPlugin {
    companion object {
        val SPRITE_SHEET = WRConstants.id("textures/gui/emi/recipe_sprite_sheet.png")
        val WORKSTATION_STACK = EmiStack.of(WRBlocks.REDSTONE_ASSEMBLER)
        val CATEGORY = EmiRecipeCategory(
            WRConstants.id("redstone_assembler"), WORKSTATION_STACK, EmiTexture(SPRITE_SHEET, 134, 0, 16, 16)
        )
    }

    override fun register(registry: EmiRegistry) {
        WRLog.log.info("Loading Wired Redstone EMI Plugin...")

        registry.addCategory(CATEGORY)
        registry.addWorkstation(CATEGORY, WORKSTATION_STACK)

        // the redstone assembler can also be used to craft
        registry.addWorkstation(VanillaEmiRecipeCategories.CRAFTING, WORKSTATION_STACK)

        // Add all recipes to EMI
        for (recipe in registry.recipeManager.listAllOfType(RedstoneAssemblerRecipeType)) {
            SimpleRedstoneAssemblerEmiRecipe.of(recipe)?.let { registry.addRecipe(it) }
        }

        registry.addRecipeHandler(WRScreenHandlers.REDSTONE_ASSEMBLER, RedstoneAssemblerEmiRecipeHandler)
    }
}
