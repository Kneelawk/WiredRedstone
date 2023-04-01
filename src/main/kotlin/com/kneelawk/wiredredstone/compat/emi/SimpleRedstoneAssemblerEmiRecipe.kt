package com.kneelawk.wiredredstone.compat.emi

import com.kneelawk.wiredredstone.WRConstants.gui
import com.kneelawk.wiredredstone.WRConstants.tooltip
import com.kneelawk.wiredredstone.blockentity.RedstoneAssemblerBlockEntity.Companion.CRAFTING_PATTERN_HEIGHT
import com.kneelawk.wiredredstone.blockentity.RedstoneAssemblerBlockEntity.Companion.CRAFTING_PATTERN_WIDTH
import com.kneelawk.wiredredstone.blockentity.RedstoneAssemblerBlockEntity.Companion.CRAFTING_SLOT_COUNT
import com.kneelawk.wiredredstone.blockentity.RedstoneAssemblerBlockEntity.Companion.OUTPUT_HEIGHT
import com.kneelawk.wiredredstone.blockentity.RedstoneAssemblerBlockEntity.Companion.OUTPUT_WIDTH
import com.kneelawk.wiredredstone.compat.emi.WiredRedstonePlugin.Companion.SPRITE_SHEET
import com.kneelawk.wiredredstone.recipe.RedstoneAssemblerRecipe
import com.kneelawk.wiredredstone.recipe.RedstoneAssemblerShapedRecipe
import com.kneelawk.wiredredstone.recipe.RedstoneAssemblerShapelessRecipe
import com.kneelawk.wiredredstone.util.RecipeUtil
import dev.emi.emi.api.recipe.EmiRecipeCategory
import dev.emi.emi.api.render.EmiTexture
import dev.emi.emi.api.stack.EmiIngredient
import dev.emi.emi.api.stack.EmiStack
import dev.emi.emi.api.widget.WidgetHolder
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.tooltip.TooltipComponent
import net.minecraft.util.Identifier

class SimpleRedstoneAssemblerEmiRecipe(
    private val id: Identifier, override val width: Int, override val height: Int,
    private val inputs: List<EmiIngredient>, private val outputs: List<EmiStack>, private val energyPerTick: Int,
    private val cookTime: Int, private val isShapeless: Boolean
) : RedstoneAssemblerEmiRecipe {
    companion object {
        val CLIENT = MinecraftClient.getInstance()
        val BACKGROUND = EmiTexture(SPRITE_SHEET, 0, 0, 134, 62)
        val SHAPED = EmiTexture(SPRITE_SHEET, 134, 16, 36, 18)
        val SHAPELESS = EmiTexture(SPRITE_SHEET, 134, 16 + 18, 36, 18)

        fun of(recipe: RedstoneAssemblerRecipe): SimpleRedstoneAssemblerEmiRecipe? {
            return when (recipe) {
                is RedstoneAssemblerShapedRecipe -> SimpleRedstoneAssemblerEmiRecipe(
                    recipe.id, recipe.width, recipe.height, recipe.ingredients.map(EmiIngredient::of),
                    listOf(EmiStack.of(recipe.getViewerOutput())), recipe.energyPerTick, recipe.cookTime, false
                )
                is RedstoneAssemblerShapelessRecipe -> SimpleRedstoneAssemblerEmiRecipe(
                    recipe.id, CRAFTING_PATTERN_WIDTH, CRAFTING_PATTERN_HEIGHT,
                    recipe.ingredients.map(EmiIngredient::of), listOf(EmiStack.of(recipe.getViewerOutput())),
                    recipe.energyPerTick,
                    recipe.cookTime, true
                )
                else -> null
            }
        }
    }

    override fun getCategory(): EmiRecipeCategory = WiredRedstonePlugin.CATEGORY

    override fun getId(): Identifier = id

    override fun getInputs(): List<EmiIngredient> = inputs

    override fun getOutputs(): List<EmiStack> = outputs

    override fun getDisplayWidth(): Int = 7 * 18

    override fun getDisplayHeight(): Int = 3 * 18

    override fun addWidgets(widgets: WidgetHolder) {
        widgets.addTexture(BACKGROUND, -4, -4)

        if (isShapeless) {
            widgets.addTexture(SHAPELESS, 3 * 18, 18).tooltip { _, _ ->
                listOf(TooltipComponent.of(tooltip("redstone_assembler.shapeless").asOrderedText()))
            }
        } else {
            widgets.addTexture(SHAPED, 3 * 18, 18).tooltip { _, _ ->
                listOf(TooltipComponent.of(tooltip("redstone_assembler.shaped").asOrderedText()))
            }
        }

        val inputStacks = Array<EmiIngredient>(CRAFTING_SLOT_COUNT) { EmiStack.EMPTY }

        for (i in inputs.indices) {
            val slot = RecipeUtil.recipeToSlotIndex(i, width, height, CRAFTING_PATTERN_WIDTH)
            inputStacks[slot] = inputs[i]
        }

        for (y in 0 until CRAFTING_PATTERN_HEIGHT) {
            for (x in 0 until CRAFTING_PATTERN_WIDTH) {
                val index = x + y * CRAFTING_PATTERN_WIDTH
                widgets.addSlot(inputStacks[index], x * 18, y * 18).drawBack(false)
            }
        }

        for (y in 0 until OUTPUT_HEIGHT) {
            for (x in 0 until OUTPUT_WIDTH) {
                val index = x + y * OUTPUT_WIDTH
                val stack = if (index < outputs.size) {
                    outputs[index]
                } else {
                    EmiStack.EMPTY
                }

                val slot = widgets.addSlot(stack, 5 * 18 + x * 18, 9 + y * 18).drawBack(false)

                if (!stack.isEmpty) {
                    slot.recipeContext(this)
                }
            }
        }

        val cookTimeOrdered = gui("redstone_assembler.cook_time", cookTime).asOrderedText()
        val cookTimeWidth = CLIENT.textRenderer.getWidth(cookTimeOrdered)
        widgets.addText(cookTimeOrdered, 4 * 18 - cookTimeWidth / 2, 2 * 18, 0xFFFFFFFFu.toInt(), false)

        val energyPerTickOrdered = gui("redstone_assembler.energy_per_tick", energyPerTick).asOrderedText()
        val energyPerTickWidth = CLIENT.textRenderer.getWidth(energyPerTickOrdered)
        widgets.addText(energyPerTickOrdered, 4 * 18 - energyPerTickWidth / 2, 2 * 18 + 10, 0xFFFFFFFFu.toInt(), false)
    }
}
