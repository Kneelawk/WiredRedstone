package com.kneelawk.wiredredstone.rei

import com.kneelawk.wiredredstone.WRConstants.gui
import com.kneelawk.wiredredstone.WRConstants.id
import com.kneelawk.wiredredstone.WRConstants.tt
import com.kneelawk.wiredredstone.block.WRBlocks
import com.kneelawk.wiredredstone.blockentity.RedstoneAssemblerBlockEntity.Companion.CRAFTING_PATTERN_HEIGHT
import com.kneelawk.wiredredstone.blockentity.RedstoneAssemblerBlockEntity.Companion.CRAFTING_PATTERN_WIDTH
import com.kneelawk.wiredredstone.blockentity.RedstoneAssemblerBlockEntity.Companion.OUTPUT_HEIGHT
import com.kneelawk.wiredredstone.blockentity.RedstoneAssemblerBlockEntity.Companion.OUTPUT_WIDTH
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
    private val TITLE = tt("container", "redstone_assembler")

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
        val originX = bounds.centerX - 63
        val originY = bounds.centerY - 27

        val input = display.input
        val output = display.output

        val widgets = mutableListOf<Widget>()
        val inputSlots = mutableListOf<Slot>()

        widgets += Widgets.createRecipeBase(bounds).color(0xFF383838u.toInt(), 0xFF383838u.toInt())

        widgets += Widgets.createTexturedWidget(
            id("textures/gui/container/redstone_assembler.png"), originX, originY, 34f, 16f, 54, 54
        )

        for (y in 0 until CRAFTING_PATTERN_HEIGHT) {
            for (x in 0 until CRAFTING_PATTERN_WIDTH) {
                val widget = Widgets.createSlot(Point(originX + 1 + x * 18, originY + 1 + y * 18)).disableBackground()

                widgets.add(widget)
                inputSlots.add(widget)
            }
        }

        for (i in input.indices) {
            val slot = RecipeUtil.recipeToSlotIndex(i, display.width, display.height, CRAFTING_PATTERN_WIDTH)
            inputSlots[slot].markInput().entries(input[i])
        }

        widgets += Widgets.createTexturedWidget(
            id("textures/gui/container/redstone_assembler.png"), originX + 90, originY + 9, 124f, 16f, 36, 36
        )

        for (y in 0 until OUTPUT_HEIGHT) {
            for (x in 0 until OUTPUT_WIDTH) {
                val index = x + y * OUTPUT_WIDTH
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
            Widgets.createArrow(Point(originX + 54 + 6, originY + 18))
                .animationDurationTicks(display.cookTime.toDouble())
        )

        widgets.add(
            Widgets.createLabel(
                Point(originX + 72, originY + 36), gui("redstone_assembler.cook_time", display.cookTime)
            ).noShadow()
        )
        widgets.add(
            Widgets.createLabel(
                Point(originX + 72, originY + 36 + 10),
                gui("redstone_assembler.energy_per_tick", display.energyPerTick)
            ).noShadow()
        )

        if (display.isShapeless) {
            widgets.add(Widgets.createShapelessIcon(bounds))
        }

        return widgets
    }
}
