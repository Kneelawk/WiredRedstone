package com.kneelawk.wiredredstone.compat.rei

import com.kneelawk.wiredredstone.recipe.RedstoneAssemblerRecipe
import com.kneelawk.wiredredstone.recipe.RedstoneAssemblerShapedRecipe
import com.kneelawk.wiredredstone.recipe.RedstoneAssemblerShapelessRecipe
import me.shedaniel.rei.api.common.category.CategoryIdentifier
import me.shedaniel.rei.api.common.display.Display
import me.shedaniel.rei.api.common.display.DisplaySerializer
import me.shedaniel.rei.api.common.entry.EntryIngredient
import me.shedaniel.rei.api.common.util.EntryIngredients
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList

class RedstoneAssemblerDisplay(
    val width: Int, val height: Int, val input: List<EntryIngredient>,
    val output: List<EntryIngredient>, val energyPerTick: Int, val cookTime: Int, val isShapeless: Boolean
) : Display {
    companion object {
        fun of(recipe: RedstoneAssemblerRecipe): RedstoneAssemblerDisplay? {
            return when (recipe) {
                is RedstoneAssemblerShapedRecipe -> RedstoneAssemblerDisplay(
                    recipe.width, recipe.height, EntryIngredients.ofIngredients(recipe.ingredients),
                    listOf(EntryIngredients.of(recipe.output)), recipe.energyPerTick, recipe.cookTime, false
                )
                is RedstoneAssemblerShapelessRecipe -> RedstoneAssemblerDisplay(
                    3, 3, EntryIngredients.ofIngredients(recipe.ingredients),
                    listOf(EntryIngredients.of(recipe.output)), recipe.energyPerTick, recipe.cookTime, true
                )
                else -> null
            }
        }
    }

    override fun getInputEntries(): List<EntryIngredient> = input

    override fun getOutputEntries(): List<EntryIngredient> = output

    override fun getCategoryIdentifier(): CategoryIdentifier<*> = WiredRedstoneREI.REDSTONE_ASSEMBLER_CATEGORY

    object Serializer : DisplaySerializer<RedstoneAssemblerDisplay> {
        override fun save(tag: NbtCompound, display: RedstoneAssemblerDisplay): NbtCompound {
            with(display) {
                tag.putByte("width", width.toByte())
                tag.putByte("height", height.toByte())

                val inputList = NbtList()
                for (i in input) {
                    inputList.add(i.save())
                }
                tag.put("input", inputList)

                val outputList = NbtList()
                for (o in output) {
                    outputList.add(o.save())
                }
                tag.put("output", outputList)

                tag.putInt("energyPerTick", energyPerTick)
                tag.putInt("cookTime", cookTime)
                tag.putBoolean("shapeless", isShapeless)
            }

            return tag
        }

        override fun read(tag: NbtCompound): RedstoneAssemblerDisplay {
            val width = tag.getByte("width").toInt()
            val height = tag.getByte("height").toInt()

            val input = tag.getList("input", NbtElement.LIST_TYPE.toInt()).map { EntryIngredient.read(it as NbtList) }
            val output = tag.getList("output", NbtElement.LIST_TYPE.toInt()).map { EntryIngredient.read(it as NbtList) }

            val energyPerTick = tag.getInt("energyPerTick")
            val cookTime = tag.getInt("cookTime")
            val shapeless = tag.getBoolean("shapeless")

            return RedstoneAssemblerDisplay(width, height, input, output, energyPerTick, cookTime, shapeless)
        }
    }
}
