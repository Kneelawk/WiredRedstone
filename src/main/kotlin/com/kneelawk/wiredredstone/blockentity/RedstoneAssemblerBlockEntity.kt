package com.kneelawk.wiredredstone.blockentity

import com.kneelawk.wiredredstone.WRConstants.tt
import com.kneelawk.wiredredstone.block.RedstoneAssemblerBlock
import com.kneelawk.wiredredstone.recipe.*
import com.kneelawk.wiredredstone.screenhandler.RedstoneAssemblerScreenHandler
import com.kneelawk.wiredredstone.util.toArray
import com.kneelawk.wiredredstone.util.toByte
import com.kneelawk.wiredredstone.util.toEnum
import com.kneelawk.wiredredstone.util.toInt
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.entity.AbstractFurnaceBlockEntity
import net.minecraft.block.entity.LockableContainerBlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventories
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.SidedInventory
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.recipe.*
import net.minecraft.screen.NamedScreenHandlerFactory
import net.minecraft.screen.PropertyDelegate
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.text.Text
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.MathHelper
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
        const val CRAFTING_PATTERN_WIDTH = 3
        const val CRAFTING_PATTERN_HEIGHT = 3
        const val OUTPUT_WIDTH = 2
        const val OUTPUT_HEIGHT = 2

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

        const val DEFAULT_COOK_TIME = 200
        const val CRAFTING_COOK_TIME = 10
        const val CRAFTING_ENERGY_PER_TICK = 5

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
                val wasBurning = isBurning()
                var markDirty = false

                // generate more energy while we're burning
                if (isBurning()) {
                    burnTime--
                    energyStorage.amount =
                        MathHelper.clamp(energyStorage.amount + BURN_ENERGY_PER_TICK, 0L, ENERGY_CAPACITY)

                    markDirty = true
                }

                // consume fuel items to start the burnTime countdown again
                if (!isBurning() && isFuelItem(inventory[FUEL_SLOT])) {
                    val fuel = inventory[FUEL_SLOT]
                    burnTimeTotal = getFuelTime(fuel)
                    burnTime = burnTimeTotal

                    if (isBurning()) {
                        val item = fuel.item
                        fuel.decrement(1)
                        if (fuel.isEmpty) {
                            inventory[FUEL_SLOT] =
                                item.recipeRemainder?.let { ItemStack(it) } ?: ItemStack.EMPTY
                        }
                    }

                    markDirty = true
                }

                // step recipes along, attempting to complete them if possible
                if (hasEnergy()) {
                    when (mode) {
                        Mode.ASSEMBLER -> {
                            val recipe = getAssemblerRecipe()

                            if (recipe != null && energyStorage.amount >= recipe.energyPerTick) {
                                tryCraft(getValidAssemblerInputInventory(recipe), recipe, recipe.energyPerTick)
                            } else {
                                tryDecrementCookTime()
                            }
                        }
                        Mode.CRAFTING_TABLE -> {
                            val recipe = getCraftingRecipe()

                            if (recipe != null && energyStorage.amount >= CRAFTING_ENERGY_PER_TICK) {
                                tryCraft(getValidCraftingInputInventory(recipe), recipe, CRAFTING_ENERGY_PER_TICK)
                            } else {
                                tryDecrementCookTime()
                            }
                        }
                    }
                } else {
                    tryDecrementCookTime()
                }

                // update the `LIT` blockstate
                if (wasBurning != isBurning()) {
                    world.setBlockState(pos, state.with(RedstoneAssemblerBlock.LIT, isBurning()), Block.NOTIFY_ALL)
                    markDirty = true
                }

                if (markDirty) {
                    markDirty()
                }
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
        private set
    var burnTimeTotal = 0
        private set
    var cookTime = 0
        private set
    var cookTimeTotal = 0
        private set
    var useCraftingItems = false
        private set
    var mode = Mode.ASSEMBLER
        private set

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

    private val craftingInventory = DelegatingCraftingInventory(this, 3, 3, CRAFTING_START_SLOT)

    override val width = 3
    override val height = 3

    fun updateUseCraftingItems(use: Boolean) {
        useCraftingItems = use
        markDirty()
    }

    fun updateMode(mode: Mode) {
        this.mode = mode
        cookTimeTotal = recipeCookTimeTotal()
        cookTime = 0
        markDirty()
    }

    fun isBurning(): Boolean = burnTime > 0

    fun hasEnergy(): Boolean = energyStorage.amount > 0

    fun tryDecrementCookTime() {
        if (cookTime > 0) {
            cookTime = MathHelper.clamp(cookTime - 2, 0, cookTimeTotal)
            markDirty()
        }
    }

    fun getAssemblerRecipe(): RedstoneAssemblerRecipe? =
        world!!.recipeManager.getFirstMatch(RedstoneAssemblerRecipeType, this, world).orElse(null)

    fun getCraftingRecipe(): CraftingRecipe? =
        world!!.recipeManager.getFirstMatch(RecipeType.CRAFTING, craftingInventory, world).orElse(null)

    fun getValidAssemblerInputInventory(recipe: RedstoneAssemblerRecipe): DelegateSlotRedstoneAssemblerInventory? {
        var inventory = DelegateSlotRedstoneAssemblerInventory.fromPatternAndInput(
            this, CRAFTING_START_SLOT, CRAFTING_PATTERN_WIDTH, CRAFTING_PATTERN_HEIGHT,
            INPUT_START_SLOT, INPUT_STOP_SLOT, useCraftingItems, preferExact = false
        )

        if (inventory != null && recipe.matches(inventory, world)) {
            return inventory
        }

        if (!useCraftingItems) {
            return null
        }

        inventory = DelegateSlotRedstoneAssemblerInventory.fromPatternAndInput(
            this, CRAFTING_START_SLOT, CRAFTING_PATTERN_WIDTH, CRAFTING_PATTERN_HEIGHT,
            INPUT_START_SLOT, INPUT_STOP_SLOT, usePatternSlots = true, preferExact = false
        )

        return if (inventory != null && recipe.matches(inventory, world)) {
            inventory
        } else {
            null
        }
    }

    fun getValidCraftingInputInventory(recipe: CraftingRecipe): DelegateSlotCraftingInventory? {
        var inventory = DelegateSlotCraftingInventory.fromPatternAndInput(
            this, CRAFTING_START_SLOT, CRAFTING_PATTERN_WIDTH, CRAFTING_PATTERN_HEIGHT,
            INPUT_START_SLOT, INPUT_STOP_SLOT, useCraftingItems, preferExact = false
        )

        if (inventory != null && recipe.matches(inventory, world)) {
            return inventory
        }

        if (!useCraftingItems) {
            return null
        }

        inventory = DelegateSlotCraftingInventory.fromPatternAndInput(
            this, CRAFTING_START_SLOT, CRAFTING_PATTERN_WIDTH, CRAFTING_PATTERN_HEIGHT,
            INPUT_START_SLOT, INPUT_STOP_SLOT, usePatternSlots = true, preferExact = false
        )

        return if (inventory != null && recipe.matches(inventory, world)) {
            inventory
        } else {
            null
        }
    }

    fun <I : Inventory> tryCraft(inventory: I?, recipe: Recipe<I>, energyPerTick: Int) {
        if (inventory != null) {
            val output = recipe.craft(inventory)

            if (canAcceptOutput(output)) {
                energyStorage.amount -= energyPerTick
                cookTime++

                if (cookTime >= cookTimeTotal) {
                    cookTime = 0
                    cookTimeTotal = recipeCookTimeTotal()

                    // Rust could have this checked at compile time :(
                    if (inventory is InputTaker)
                        inventory.takeInputs()
                    insertOutput(output)
                }

                markDirty()
            }
        } else {
            tryDecrementCookTime()
        }
    }

    fun canAcceptOutput(output: ItemStack): Boolean {
        for (i in OUTPUT_START_SLOT until OUTPUT_STOP_SLOT) {
            val existing = inventory[i]
            if (existing.isEmpty) {
                return true
            }

            if (output.isStackable && ItemStack.canCombine(existing, output)) {
                if (existing.count + output.count <= existing.maxCount) {
                    return true
                } else if (existing.count < existing.maxCount) {
                    output.decrement(existing.maxCount - existing.count)
                }
            }

            if (output.isEmpty) {
                return true
            }
        }

        return false
    }

    fun insertOutput(output: ItemStack) {
        if (output.isEmpty) {
            return
        }

        // first pass tries to insert into existing stacks
        if (output.isStackable) {
            for (i in OUTPUT_START_SLOT until OUTPUT_STOP_SLOT) {
                val existing = inventory[i]

                if (!existing.isEmpty && ItemStack.canCombine(existing, output)) {
                    val total = existing.count + output.count
                    if (total <= existing.maxCount) {
                        output.count = 0
                        existing.count = total
                    } else if (existing.count < existing.maxCount) {
                        output.decrement(existing.maxCount - existing.count)
                        existing.count = existing.maxCount
                    }
                }

                if (output.isEmpty) {
                    return
                }
            }
        }

        // second pass tries to insert into empty slots
        for (i in OUTPUT_START_SLOT until OUTPUT_STOP_SLOT) {
            if (inventory[i].isEmpty) {
                inventory[i] = output.split(output.count)
            }
        }
    }

    fun recipeCookTimeTotal(): Int = if (mode == Mode.ASSEMBLER) {
        getAssemblerRecipe()?.cookTime ?: DEFAULT_COOK_TIME
    } else CRAFTING_COOK_TIME

    override fun readNbt(nbt: NbtCompound) {
        super.readNbt(nbt)
        inventory.clear()
        Inventories.readNbt(nbt, inventory)
        burnTime = nbt.getShort("BurnTime").toInt()
        burnTimeTotal = nbt.getShort("BurnTimeTotal").toInt()
        cookTime = nbt.getShort("CookTime").toInt()
        cookTimeTotal = nbt.getShort("CookTimeTotal").toInt()
        energyStorage.amount = nbt.getInt("Energy").toLong()
        useCraftingItems = nbt.getBoolean("UseCraftingItems")
        mode = nbt.getByte("Mode").toEnum()
    }

    override fun writeNbt(nbt: NbtCompound) {
        super.writeNbt(nbt)
        nbt.putShort("BurnTime", burnTime.toShort())
        nbt.putShort("BurnTimeTotal", burnTimeTotal.toShort())
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
            cookTimeTotal = recipeCookTimeTotal()
            cookTime = 0
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
        if (useCraftingItems) {
            for (i in CRAFTING_START_SLOT until CRAFTING_STOP_SLOT) {
                finder.addInput(inventory[i])
            }
        }

        for (i in INPUT_START_SLOT until INPUT_STOP_SLOT) {
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
