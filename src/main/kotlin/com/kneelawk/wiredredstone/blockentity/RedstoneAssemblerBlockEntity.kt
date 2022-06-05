package com.kneelawk.wiredredstone.blockentity

import com.kneelawk.wiredredstone.WRConstants.tt
import com.kneelawk.wiredredstone.recipe.RedstoneAssemblerInventory
import com.kneelawk.wiredredstone.screenhandler.RedstoneAssemblerScreenHandler
import com.kneelawk.wiredredstone.util.toArray
import com.kneelawk.wiredredstone.util.toByte
import com.kneelawk.wiredredstone.util.toEnum
import com.kneelawk.wiredredstone.util.toInt
import net.minecraft.block.BlockState
import net.minecraft.block.entity.AbstractFurnaceBlockEntity
import net.minecraft.block.entity.LockableContainerBlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventories
import net.minecraft.inventory.SidedInventory
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.recipe.RecipeInputProvider
import net.minecraft.recipe.RecipeMatcher
import net.minecraft.screen.NamedScreenHandlerFactory
import net.minecraft.screen.PropertyDelegate
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.text.Text
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World
import team.reborn.energy.api.base.SimpleEnergyStorage

class RedstoneAssemblerBlockEntity(pos: BlockPos, state: BlockState) :
    LockableContainerBlockEntity(WRBlockEntities.REDSTONE_ASSEMBLER, pos, state), NamedScreenHandlerFactory,
    SidedInventory, RecipeInputProvider, RedstoneAssemblerInventory {
    companion object {
        const val CRAFTING_START_SLOT = 0
        const val CRAFTING_STOP_SLOT = 9
        const val OUTPUT_START_SLOT = 9
        const val OUTPUT_STOP_SLOT = 13
        const val FUEL_SLOT = 13
        const val INPUT_START_SLOT = 14
        const val INPUT_STOP_SLOT = 32
        const val SLOT_COUNT = 32
        const val CRAFTING_SLOT_COUNT = 9
        const val OUTPUT_SLOT_COUNT = 4

        const val BURN_TIME_PROPERTY = 0
        const val BURN_TIME_TOTAL_PROPERTY = 1
        const val COOK_TIME_PROPERTY = 2
        const val COOK_TIME_TOTAL_PROPERTY = 3
        const val ENERGY_PROPERTY = 4
        const val USE_CRAFTING_ITEMS_PROPERTY = 5
        const val MODE_PROPERTY = 6
        const val PROPERTY_COUNT = 7

        const val ENERGY_CAPACITY = 128000L
        const val ENERGY_MAX_INSERT = 128L
        const val ENERGY_MAX_EXTRACT = 128L

        const val BURN_ENERGY_PER_TICK = 5
        val FUEL_TIME_MAP: Map<Item, Int> by lazy {
            AbstractFurnaceBlockEntity.createFuelTimeMap().mapValues { it.value / 2 }
        }

        val TOP_SLOTS = (INPUT_START_SLOT until INPUT_STOP_SLOT).toArray()
        val SIDE_SLOTS = intArrayOf(FUEL_SLOT)
        val BOTTOM_SLOTS = (OUTPUT_START_SLOT until OUTPUT_STOP_SLOT).toArray()

        private val NAME = tt("container", "redstone_assembler")

        fun tick(world: World, pos: BlockPos, state: BlockState, blockEntity: RedstoneAssemblerBlockEntity) {
            with(blockEntity) {

            }
        }

        fun getFuelTime(fuel: ItemStack): Int {
            return if (fuel.isEmpty) {
                0
            } else {
                val item = fuel.item
                FUEL_TIME_MAP.getOrDefault(item, 0)
            }
        }

        fun isFuelItem(fuel: ItemStack): Boolean {
            return !fuel.isEmpty && FUEL_TIME_MAP.containsKey(fuel.item)
        }
    }

    enum class Mode {
        ASSEMBLER, CRAFTING_TABLE;
    }

    val inventory: DefaultedList<ItemStack> = DefaultedList.ofSize(SLOT_COUNT, ItemStack.EMPTY)
    val energyStorage = SimpleEnergyStorage(ENERGY_CAPACITY, ENERGY_MAX_INSERT, ENERGY_MAX_EXTRACT)

    var burnTime = 0
    var burnTimeTotal = 0
    var cookTime = 0
    var cookTimeTotal = 0
    var useCraftingItems = true
    var mode = Mode.ASSEMBLER

    private val propertyDelegate = object : PropertyDelegate {
        override fun get(index: Int): Int {
            return when (index) {
                BURN_TIME_PROPERTY -> burnTime
                BURN_TIME_TOTAL_PROPERTY -> burnTimeTotal
                COOK_TIME_PROPERTY -> cookTime
                COOK_TIME_TOTAL_PROPERTY -> cookTimeTotal
                ENERGY_PROPERTY -> energyStorage.amount.toInt()
                USE_CRAFTING_ITEMS_PROPERTY -> if (useCraftingItems) 1 else 0
                MODE_PROPERTY -> mode.toInt()
                else -> 0
            }
        }

        override fun set(index: Int, value: Int) {
            when (index) {
                BURN_TIME_PROPERTY -> burnTime = value
                BURN_TIME_TOTAL_PROPERTY -> burnTimeTotal = value
                COOK_TIME_PROPERTY -> cookTime = value
                COOK_TIME_TOTAL_PROPERTY -> cookTimeTotal = value
                ENERGY_PROPERTY -> energyStorage.amount = value.toLong()
                USE_CRAFTING_ITEMS_PROPERTY -> useCraftingItems = value != 0
                MODE_PROPERTY -> mode = value.toEnum()
            }
        }

        override fun size(): Int = PROPERTY_COUNT
    }

    override val width = 3
    override val height = 3

    override fun readNbt(nbt: NbtCompound) {
        super.readNbt(nbt)
        inventory.clear()
        Inventories.readNbt(nbt, inventory)
        burnTime = nbt.getShort("BurnTime").toInt()
        cookTime = nbt.getShort("CookTime").toInt()
        cookTimeTotal = nbt.getShort("CookTimeTotal").toInt()
        energyStorage.amount = nbt.getInt("Energy").toLong()
        useCraftingItems = nbt.getBoolean("UseCraftingItems")
        mode = nbt.getByte("Mode").toEnum()
        burnTimeTotal = getFuelTime(inventory[FUEL_SLOT])
    }

    override fun writeNbt(nbt: NbtCompound) {
        super.writeNbt(nbt)
        nbt.putShort("BurnTime", burnTime.toShort())
        nbt.putShort("CookTime", cookTime.toShort())
        nbt.putShort("CookTimeTotal", cookTimeTotal.toShort())
        nbt.putInt("Energy", energyStorage.amount.toInt())
        nbt.putBoolean("UseCraftingItems", useCraftingItems)
        nbt.putByte("Mode", mode.toByte())
        Inventories.writeNbt(nbt, inventory)
    }

    override fun clear() = inventory.clear()

    override fun size(): Int = inventory.size

    override fun isEmpty(): Boolean {
        for (itemStack in inventory) {
            if (!itemStack.isEmpty) {
                return false
            }
        }

        return true
    }

    override fun getStack(slot: Int): ItemStack = inventory[slot]

    override fun removeStack(slot: Int, amount: Int): ItemStack = Inventories.splitStack(inventory, slot, amount)

    override fun removeStack(slot: Int): ItemStack = Inventories.removeStack(inventory, slot)

    override fun setStack(slot: Int, stack: ItemStack) {
        val itemStack = inventory[slot]
        val sameStack =
            !stack.isEmpty && stack.isItemEqualIgnoreDamage(itemStack) && ItemStack.areNbtEqual(stack, itemStack)
        inventory[slot] = stack
        if (stack.count > this.maxCountPerStack) {
            stack.count = this.maxCountPerStack
        }

        if (slot in CRAFTING_START_SLOT until CRAFTING_STOP_SLOT && !sameStack) {
            // TODO: recipe start detection
        }

        this.markDirty()
    }

    override fun canPlayerUse(player: PlayerEntity): Boolean {
        return if (world!!.getBlockEntity(pos) !== this) {
            false
        } else {
            player.squaredDistanceTo(pos.x.toDouble() + 0.5, pos.y.toDouble() + 0.5, pos.z.toDouble() + 0.5) <= 64.0
        }
    }

    override fun getContainerName(): Text = NAME

    override fun createScreenHandler(syncId: Int, playerInventory: PlayerInventory): ScreenHandler {
        return RedstoneAssemblerScreenHandler(
            syncId, playerInventory, this, propertyDelegate, ScreenHandlerContext.create(world!!, pos)
        )
    }

    override fun getAvailableSlots(side: Direction): IntArray {
        return if (side == Direction.DOWN) {
            BOTTOM_SLOTS
        } else {
            if (side == Direction.UP) TOP_SLOTS else SIDE_SLOTS
        }
    }

    override fun canInsert(slot: Int, stack: ItemStack, dir: Direction?): Boolean = isValid(slot, stack)

    override fun isValid(slot: Int, stack: ItemStack): Boolean {
        return when (slot) {
            in CRAFTING_START_SLOT until CRAFTING_STOP_SLOT -> true
            in OUTPUT_START_SLOT until OUTPUT_STOP_SLOT -> false
            FUEL_SLOT -> {
                val itemStack = inventory[FUEL_SLOT]
                AbstractFurnaceBlockEntity.canUseAsFuel(stack) || stack.isOf(Items.BUCKET) && !itemStack.isOf(
                    Items.BUCKET
                )
            }
            in INPUT_START_SLOT until INPUT_STOP_SLOT -> true
            else -> false
        }
    }

    override fun canExtract(slot: Int, stack: ItemStack, dir: Direction): Boolean {
        return if (dir == Direction.DOWN && slot == FUEL_SLOT) {
            stack.isOf(Items.WATER_BUCKET) || stack.isOf(Items.BUCKET)
        } else {
            true
        }
    }

    override fun provideRecipeInputs(finder: RecipeMatcher) {
        for (i in CRAFTING_START_SLOT until CRAFTING_STOP_SLOT) {
            finder.addInput(inventory[i])
        }
    }

    override fun getCraftingStack(x: Int, y: Int): ItemStack {
        return if (x in 0 until width && y in 0 until height) {
            inventory[CRAFTING_START_SLOT + x + y * width]
        } else {
            ItemStack.EMPTY
        }
    }

    override fun clearCraftingSlots() {
        for (i in CRAFTING_START_SLOT until CRAFTING_STOP_SLOT) {
            inventory[i] = ItemStack.EMPTY
        }
    }
}
