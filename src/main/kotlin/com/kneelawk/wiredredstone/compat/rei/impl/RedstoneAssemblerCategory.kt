package com.kneelawk.wiredredstone.compat.rei.impl

import com.kneelawk.wiredredstone.WRConstants
import com.kneelawk.wiredredstone.block.WRBlocks
import com.kneelawk.wiredredstone.blockentity.RedstoneAssemblerBlockEntity
import com.kneelawk.wiredredstone.util.RecipeUtil
import me.shedaniel.math.Point
import me.shedaniel.math.Rectangle
import me.shedaniel.rei.api.client.gui.Renderer
import me.shedaniel.rei.api.client.gui.widgets.Slot
import me.shedaniel.rei.api.client.gui.widgets.Widget
import me.shedaniel.rei.api.client.gui.widgets.Widgets
import me.shedaniel.rei.api.client.registry.display.DisplayCategory
import me.shedaniel.rei.api.common.category.CategoryIdentifier
import me.shedaniel.rei.api.common.util.EntryStacks
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.text.Text

@Environment(EnvType.CLIENT)
object RedstoneAssemblerCategory : DisplayCategory<RedstoneAssemblerDisplay> {
    private val TITLE = WRConstants.tt("container", "redstone_assembler")
    private val SPRITE_SHEET = WRConstants.id("textures/gui/rei/recipe_sprite_sheet.png")
    private val SHAPELESS_TOOLTIP = WRConstants.tooltip("redstone_assembler.shapeless")
    private val SHAPED_TOOLTIP = WRConstants.tooltip("redstone_assembler.shaped")

    override fun getIcon(): Renderer {
        return EntryStacks.of(WRBlocks.REDSTONE_ASSEMBLER)
    }

    override fun getTitle(): Text {
        return TITLE
    }

    override fun getCategoryIdentifier(): CategoryIdentifier<out RedstoneAssemblerDisplay> {
        return WiredRedstoneREI.REDSTONE_ASSEMBLER_CATEGORY
    }

    override fun setupDisplay(display: RedstoneAssemblerDisplay, bounds: Rectangle): List<Widget> {
        val originX = bounds.x + 12
        val originY = bounds.y + 6

        val input = display.input
        val output = display.output

        val widgets = mutableListOf<Widget>()
        val inputSlots = mutableListOf<Slot>()

        widgets += Widgets.createTexturedWidget(SPRITE_SHEET, bounds.x, bounds.y, 0f, 0f, 150, 66)

        val arrowBounds = Rectangle(originX + 3 * 18, originY + 18, 2 * 18, 18)
        widgets += if (display.isShapeless) {
            Widgets.withTooltip(
                Widgets.withBounds(Widgets.createTexturedWidget(SPRITE_SHEET, arrowBounds, 150f, 18f), arrowBounds),
                SHAPELESS_TOOLTIP
            )
        } else {
            Widgets.withTooltip(
                Widgets.withBounds(Widgets.createTexturedWidget(SPRITE_SHEET, arrowBounds, 150f, 0f), arrowBounds),
                SHAPED_TOOLTIP
            )
        }

        for (y in 0 until RedstoneAssemblerBlockEntity.CRAFTING_PATTERN_HEIGHT) {
            for (x in 0 until RedstoneAssemblerBlockEntity.CRAFTING_PATTERN_WIDTH) {
                val widget = Widgets.createSlot(Point(originX + 1 + x * 18, originY + 1 + y * 18)).disableBackground()

                widgets.add(widget)
                inputSlots.add(widget)
            }
        }

        for (i in input.indices) {
            val slot = RecipeUtil.recipeToSlotIndex(
                i, display.width, display.height, RedstoneAssemblerBlockEntity.CRAFTING_PATTERN_WIDTH
            )
            inputSlots[slot].markInput().entries(input[i])
        }

        for (y in 0 until RedstoneAssemblerBlockEntity.OUTPUT_HEIGHT) {
            for (x in 0 until RedstoneAssemblerBlockEntity.OUTPUT_WIDTH) {
                val index = x + y * RedstoneAssemblerBlockEntity.OUTPUT_WIDTH
                val widget = Widgets.createSlot(Point(originX + 1 + 90 + x * 18, originY + 1 + 9 + y * 18))
                    .disableBackground()
                    .markOutput()

                if (index < output.size && !output[index].isEmpty()) {
                    widget.entries(output[index])
                }

                widgets.add(widget)
            }
        }

        widgets.add(
            Widgets.createLabel(
                Point(originX + 72, originY + 36), WRConstants.gui("redstone_assembler.cook_time", display.cookTime)
            ).noShadow()
        )
        widgets.add(
            Widgets.createLabel(
                Point(originX + 72, originY + 36 + 10),
                WRConstants.gui("redstone_assembler.energy_per_tick", display.energyPerTick)
            ).noShadow()
        )

        return widgets
    }
}
