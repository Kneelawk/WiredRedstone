package com.kneelawk.wiredredstone.screenhandler

import alexiil.mc.lib.net.impl.McNetworkStack
import com.kneelawk.wiredredstone.WRConstants.str
import com.kneelawk.wiredredstone.block.WRBlocks
import com.kneelawk.wiredredstone.blockentity.RedstoneAssemblerBlockEntity
import com.kneelawk.wiredredstone.blockentity.RedstoneAssemblerBlockEntity.Companion.BURN_TIME_PROPERTY
import com.kneelawk.wiredredstone.blockentity.RedstoneAssemblerBlockEntity.Companion.BURN_TIME_TOTAL_PROPERTY
import com.kneelawk.wiredredstone.blockentity.RedstoneAssemblerBlockEntity.Companion.COOK_TIME_PROPERTY
import com.kneelawk.wiredredstone.blockentity.RedstoneAssemblerBlockEntity.Companion.COOK_TIME_TOTAL_PROPERTY
import com.kneelawk.wiredredstone.blockentity.RedstoneAssemblerBlockEntity.Companion.CRAFTING_PATTERN_HEIGHT
import com.kneelawk.wiredredstone.blockentity.RedstoneAssemblerBlockEntity.Companion.CRAFTING_PATTERN_WIDTH
import com.kneelawk.wiredredstone.blockentity.RedstoneAssemblerBlockEntity.Companion.CRAFTING_SLOT_COUNT
import com.kneelawk.wiredredstone.blockentity.RedstoneAssemblerBlockEntity.Companion.CRAFTING_START_SLOT
import com.kneelawk.wiredredstone.blockentity.RedstoneAssemblerBlockEntity.Companion.ENERGY_HIGH_PROPERTY
import com.kneelawk.wiredredstone.blockentity.RedstoneAssemblerBlockEntity.Companion.ENERGY_LOW_PROPERTY
import com.kneelawk.wiredredstone.blockentity.RedstoneAssemblerBlockEntity.Companion.FUEL_SLOT
import com.kneelawk.wiredredstone.blockentity.RedstoneAssemblerBlockEntity.Companion.INPUT_START_SLOT
import com.kneelawk.wiredredstone.blockentity.RedstoneAssemblerBlockEntity.Companion.INPUT_STOP_SLOT
import com.kneelawk.wiredredstone.blockentity.RedstoneAssemblerBlockEntity.Companion.MODE_PROPERTY
import com.kneelawk.wiredredstone.blockentity.RedstoneAssemblerBlockEntity.Companion.OUTPUT_HEIGHT
import com.kneelawk.wiredredstone.blockentity.RedstoneAssemblerBlockEntity.Companion.OUTPUT_SLOT_COUNT
import com.kneelawk.wiredredstone.blockentity.RedstoneAssemblerBlockEntity.Companion.OUTPUT_START_SLOT
import com.kneelawk.wiredredstone.blockentity.RedstoneAssemblerBlockEntity.Companion.OUTPUT_STOP_SLOT
import com.kneelawk.wiredredstone.blockentity.RedstoneAssemblerBlockEntity.Companion.OUTPUT_WIDTH
import com.kneelawk.wiredredstone.blockentity.RedstoneAssemblerBlockEntity.Companion.PROPERTY_COUNT
import com.kneelawk.wiredredstone.blockentity.RedstoneAssemblerBlockEntity.Companion.SLOT_COUNT
import com.kneelawk.wiredredstone.blockentity.RedstoneAssemblerBlockEntity.Companion.USE_CRAFTING_ITEMS_PROPERTY
import com.kneelawk.wiredredstone.blockentity.RedstoneAssemblerBlockEntity.Companion.isFuelItem
import com.kneelawk.wiredredstone.config.AssemblerConfig
import com.kneelawk.wiredredstone.recipe.RedstoneAssemblerInventory
import com.kneelawk.wiredredstone.recipe.SimpleRedstoneAssemblerInventory
import com.kneelawk.wiredredstone.util.NetExtensions
import com.kneelawk.wiredredstone.util.setRecv
import com.kneelawk.wiredredstone.util.toByte
import com.kneelawk.wiredredstone.util.toEnum
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.recipe.Recipe
import net.minecraft.recipe.RecipeMatcher
import net.minecraft.recipe.book.RecipeBookCategory
import net.minecraft.screen.AbstractRecipeScreenHandler
import net.minecraft.screen.ArrayPropertyDelegate
import net.minecraft.screen.PropertyDelegate
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.screen.slot.Slot

class RedstoneAssemblerScreenHandler(
    syncId: Int, playerInventory: PlayerInventory, private val inventory: RedstoneAssemblerInventory,
    private val delegate: PropertyDelegate, private val context: ScreenHandlerContext
) : AbstractRecipeScreenHandler<RedstoneAssemblerInventory>(WRScreenHandlers.REDSTONE_ASSEMBLER, syncId),
    NetExtensions {
    companion object {
        fun initNetworking() {}

        private val NET_PARENT =
            McNetworkStack.SCREEN_HANDLER.subType(RedstoneAssemblerScreenHandler::class.java, str("redstone_assembler"))

        private val NET_SET_USE_CRAFTING_ITEMS =
            NET_PARENT.idData("set_use_crafting_items").toServerOnly().setRecv { buf, _ ->
                useCraftingItems = buf.readBoolean()
            }

        private val NET_SET_MODE = NET_PARENT.idData("set_mode").toServerOnly().setRecv { buf, _ ->
            mode = buf.readByte().toEnum()
        }
    }

    constructor(syncId: Int, playerInventory: PlayerInventory) : this(
        syncId, playerInventory, SimpleRedstoneAssemblerInventory(
            SLOT_COUNT, CRAFTING_PATTERN_WIDTH, CRAFTING_PATTERN_HEIGHT, CRAFTING_START_SLOT
        ),
        ArrayPropertyDelegate(PROPERTY_COUNT), ScreenHandlerContext.EMPTY
    )

    private val player = playerInventory.player
    private val fuelSlot = FuelSlot(inventory, FUEL_SLOT, 8, 71)

    init {
        checkDataCount(delegate, PROPERTY_COUNT)

        // Add all slots in the order of their inventory numbers. This will make slot index calculations easier.

        // crafting slots
        for (y in 0 until CRAFTING_PATTERN_HEIGHT) {
            for (x in 0 until CRAFTING_PATTERN_WIDTH) {
                addSlot(Slot(inventory, CRAFTING_START_SLOT + x + y * CRAFTING_PATTERN_WIDTH, 35 + x * 18, 17 + y * 18))
            }
        }

        // output slots
        for (y in 0 until OUTPUT_HEIGHT) {
            for (x in 0 until OUTPUT_WIDTH) {
                addSlot(OutputSlot(inventory, OUTPUT_START_SLOT + x + y * OUTPUT_WIDTH, 125 + x * 18, 17 + y * 18))
            }
        }

        // fuel slot
        addSlot(fuelSlot)

        // input slots
        for (y in 0 until 2) {
            for (x in 0 until 9) {
                addSlot(Slot(inventory, INPUT_START_SLOT + x + y * 9, 8 + x * 18, 93 + y * 18))
            }
        }

        // Finally, add the player inventory.

        // upper player inventory
        for (y in 0 until 3) {
            for (x in 0 until 9) {
                addSlot(Slot(playerInventory, 9 + x + y * 9, 8 + x * 18, 142 + y * 18))
            }
        }

        // player inventory hot bar
        for (x in 0 until 9) {
            addSlot(Slot(playerInventory, x, 8 + x * 18, 200))
        }

        addProperties(delegate)

        inventory.onOpen(player)
    }

    fun getCookProgress(): Int {
        val cookTime = delegate.get(COOK_TIME_PROPERTY)
        val cookTimeTotal = delegate.get(COOK_TIME_TOTAL_PROPERTY)
        return if (cookTimeTotal != 0 && cookTime != 0) cookTime * 24 / cookTimeTotal else 0
    }

    fun getFuelProgress(): Int {
        var burnTimeTotal = delegate.get(BURN_TIME_TOTAL_PROPERTY)
        if (burnTimeTotal == 0) {
            burnTimeTotal = 200
        }
        return delegate.get(BURN_TIME_PROPERTY) * 13 / burnTimeTotal
    }

    val isBurning: Boolean
        get() = delegate.get(BURN_TIME_PROPERTY) > 0

    val energyBar: Int
        get() = energyValue / (AssemblerConfig.instance.energyCapacity.toInt() / 32)

    val energyValue: Int
        get() = (delegate.get(ENERGY_LOW_PROPERTY) and 0xFFFF) or
                ((delegate.get(ENERGY_HIGH_PROPERTY) and 0xFFFF) shl 0x10)

    var useCraftingItems: Boolean
        get() = delegate.get(USE_CRAFTING_ITEMS_PROPERTY) != 0
        set(value) {
            if (player.world.isClient) {
                NET_SET_USE_CRAFTING_ITEMS.sendToServer { buf, _ -> buf.writeBoolean(value) }
            } else if (inventory is RedstoneAssemblerBlockEntity) {
                inventory.updateUseCraftingItems(value)
            }
        }

    var mode: RedstoneAssemblerBlockEntity.Mode
        get() = delegate.get(MODE_PROPERTY).toEnum()
        set(value) {
            if (player.world.isClient) {
                NET_SET_MODE.sendToServer { buf, _ -> buf.writeByte(value.toByte().toInt()) }
            } else if (inventory is RedstoneAssemblerBlockEntity) {
                inventory.updateMode(value)
            }
        }

    override fun close(player: PlayerEntity) {
        super.close(player)
        inventory.onClose(player)
    }

    override fun canUse(player: PlayerEntity): Boolean = canUse(context, player, WRBlocks.REDSTONE_ASSEMBLER)

    override fun canInsertIntoSlot(index: Int): Boolean {
        return index !in OUTPUT_START_SLOT until OUTPUT_STOP_SLOT
    }

    override fun populateRecipeFinder(finder: RecipeMatcher) {
        inventory.provideRecipeInputs(finder)
    }

    override fun clearCraftingSlots() {
        inventory.clearCraftingSlots()
    }

    override fun matches(recipe: Recipe<in RedstoneAssemblerInventory>): Boolean {
        return recipe.matches(inventory, player.world)
    }

    override fun getCraftingResultSlotIndex(): Int {
        return OUTPUT_START_SLOT
    }

    override fun getCraftingWidth(): Int {
        return inventory.width
    }

    override fun getCraftingHeight(): Int {
        return inventory.height
    }

    override fun getCraftingSlotCount(): Int {
        return CRAFTING_SLOT_COUNT + OUTPUT_SLOT_COUNT
    }

    override fun getCategory(): RecipeBookCategory {
        return RecipeBookCategory.CRAFTING
    }

    override fun quickTransfer(player: PlayerEntity, index: Int): ItemStack {
        var result = ItemStack.EMPTY

        val slot = slots[index]
        if (slot.hasStack()) {
            val stack = slot.stack
            result = stack.copy()

            if (index in OUTPUT_START_SLOT until OUTPUT_STOP_SLOT) {
                if (!insertItem(stack, SLOT_COUNT, SLOT_COUNT + 4 * 9, true)) {
                    return ItemStack.EMPTY
                }

                slot.onQuickTransfer(stack, result)
            } else if (index >= SLOT_COUNT) {
                if (isFuelItem(stack)) {
                    if (!insertItem(stack, FUEL_SLOT, FUEL_SLOT + 1, false)) {
                        return ItemStack.EMPTY
                    }
                } else {
                    if (!insertItem(stack, INPUT_START_SLOT, INPUT_STOP_SLOT, false)) {
                        return ItemStack.EMPTY
                    }
                }
            } else if (!insertItem(stack, SLOT_COUNT, SLOT_COUNT + 4 * 9, false)) {
                return ItemStack.EMPTY
            }

            inventory.markDirty()
        }

        return result
    }

    class FuelSlot(inventory: Inventory, index: Int, x: Int, y: Int) : Slot(inventory, index, x, y) {
        override fun canInsert(stack: ItemStack): Boolean {
            return isFuelItem(stack)
        }
    }

    class OutputSlot(inventory: Inventory, index: Int, x: Int, y: Int) : Slot(inventory, index, x, y) {
        override fun canInsert(stack: ItemStack?): Boolean {
            return false
        }
    }
}
