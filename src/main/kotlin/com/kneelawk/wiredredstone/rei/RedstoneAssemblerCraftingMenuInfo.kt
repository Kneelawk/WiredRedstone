package com.kneelawk.wiredredstone.rei

import com.kneelawk.wiredredstone.blockentity.RedstoneAssemblerBlockEntity
import com.kneelawk.wiredredstone.blockentity.RedstoneAssemblerBlockEntity.Companion.CRAFTING_START_SLOT
import com.kneelawk.wiredredstone.blockentity.RedstoneAssemblerBlockEntity.Companion.CRAFTING_STOP_SLOT
import com.kneelawk.wiredredstone.screenhandler.RedstoneAssemblerScreenHandler
import me.shedaniel.rei.api.common.display.SimpleGridMenuDisplay
import me.shedaniel.rei.api.common.transfer.info.MenuInfoContext
import me.shedaniel.rei.api.common.transfer.info.clean.InputCleanHandler
import me.shedaniel.rei.api.common.transfer.info.simple.SimplePlayerInventoryMenuInfo
import me.shedaniel.rei.api.common.transfer.info.stack.SlotAccessor

class RedstoneAssemblerCraftingMenuInfo<D : SimpleGridMenuDisplay>(private val display: D) :
    SimplePlayerInventoryMenuInfo<RedstoneAssemblerScreenHandler, D> {
    override fun getInputSlots(
        context: MenuInfoContext<RedstoneAssemblerScreenHandler, *, D>
    ): Iterable<SlotAccessor> {
        return (CRAFTING_START_SLOT until CRAFTING_STOP_SLOT).map { i ->
            SlotAccessor.fromSlot(context.menu.getSlot(i))
        }
    }

    override fun getDisplay(): D = display

    override fun getInputCleanHandler(): InputCleanHandler<RedstoneAssemblerScreenHandler, D> {
        return InputCleanHandler { context ->
            val handler = context.menu

            // set the handler to redstone-assembler mode in preparation for the recipe being added
            handler.mode = RedstoneAssemblerBlockEntity.Mode.CRAFTING_TABLE

            for (i in CRAFTING_START_SLOT until CRAFTING_STOP_SLOT) {
                val accessor = SlotAccessor.fromSlot(handler.getSlot(i))
                if (!accessor.itemStack.isEmpty) {
                    InputCleanHandler.returnSlotsToPlayerInventory(context, dumpHandler, accessor)
                }
            }

            clearInputSlots(handler)
        }
    }
}
