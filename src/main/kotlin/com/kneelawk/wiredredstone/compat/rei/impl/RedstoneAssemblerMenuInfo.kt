package com.kneelawk.wiredredstone.compat.rei.impl

import com.kneelawk.wiredredstone.blockentity.RedstoneAssemblerBlockEntity
import com.kneelawk.wiredredstone.screenhandler.RedstoneAssemblerScreenHandler
import com.kneelawk.wiredredstone.util.RecipeUtil
import me.shedaniel.rei.api.common.transfer.info.MenuInfoContext
import me.shedaniel.rei.api.common.transfer.info.clean.InputCleanHandler
import me.shedaniel.rei.api.common.transfer.info.simple.SimplePlayerInventoryMenuInfo
import me.shedaniel.rei.api.common.transfer.info.stack.SlotAccessor

class RedstoneAssemblerMenuInfo(private val display: RedstoneAssemblerDisplay) :
    SimplePlayerInventoryMenuInfo<RedstoneAssemblerScreenHandler, RedstoneAssemblerDisplay> {
    override fun getInputSlots(
        context: MenuInfoContext<RedstoneAssemblerScreenHandler, *, RedstoneAssemblerDisplay>
    ): Iterable<SlotAccessor> {
        val handler = context.menu
        val display = context.display

        return display.input.indices.map {
            val slot = RecipeUtil.recipeToSlotIndex(
                it, display.width, display.height, RedstoneAssemblerBlockEntity.CRAFTING_PATTERN_WIDTH
            )
            SlotAccessor.fromSlot(handler.getSlot(slot))
        }
    }

    override fun getDisplay(): RedstoneAssemblerDisplay = display

    override fun getInputCleanHandler(): InputCleanHandler<RedstoneAssemblerScreenHandler, RedstoneAssemblerDisplay> {
        return InputCleanHandler { context ->
            val handler = context.menu

            // set the handler to redstone-assembler mode in preparation for the recipe being added
            handler.mode = RedstoneAssemblerBlockEntity.Mode.ASSEMBLER

            for (i in RedstoneAssemblerBlockEntity.CRAFTING_START_SLOT until RedstoneAssemblerBlockEntity.CRAFTING_STOP_SLOT) {
                val accessor = SlotAccessor.fromSlot(handler.getSlot(i))
                if (!accessor.itemStack.isEmpty) {
                    InputCleanHandler.returnSlotsToPlayerInventory(context, dumpHandler, accessor)
                }
            }

            clearInputSlots(handler)
        }
    }
}
