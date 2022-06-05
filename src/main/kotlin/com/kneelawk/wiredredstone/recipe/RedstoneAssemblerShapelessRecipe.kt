package com.kneelawk.wiredredstone.recipe

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.recipe.Ingredient
import net.minecraft.recipe.RecipeMatcher
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.util.Identifier
import net.minecraft.util.JsonHelper
import net.minecraft.util.collection.DefaultedList
import net.minecraft.world.World

class RedstoneAssemblerShapelessRecipe(
    private val id: Identifier, private val group: String, private val output: ItemStack,
    private val input: DefaultedList<Ingredient>, override val energyPerTick: Int, override val cookTime: Int
) : RedstoneAssemblerRecipe {
    override fun getId(): Identifier = id

    override fun getGroup(): String = group

    override fun getOutput(): ItemStack = output

    override fun getIngredients(): DefaultedList<Ingredient> = input

    override fun matches(inventory: RedstoneAssemblerInventory, world: World): Boolean {
        val recipeMatcher = RecipeMatcher()
        var count = 0

        for (x in 0 until inventory.width) {
            for (y in 0 until inventory.height) {
                val itemStack: ItemStack = inventory.getCraftingStack(x, y)
                if (!itemStack.isEmpty) {
                    ++count
                    recipeMatcher.addInput(itemStack, 1)
                }
            }
        }

        return count == input.size && recipeMatcher.match(this, null)
    }

    override fun craft(inventory: RedstoneAssemblerInventory): ItemStack = output.copy()

    override fun fits(width: Int, height: Int): Boolean {
        return width * height >= input.size
    }

    override fun getSerializer(): RecipeSerializer<*> {
        return Serializer
    }

    object Serializer : RecipeSerializer<RedstoneAssemblerShapelessRecipe> {
        override fun read(id: Identifier, json: JsonObject): RedstoneAssemblerShapelessRecipe {
            val group = JsonHelper.getString(json, "group", "")!!
            val input = getIngredients(JsonHelper.getArray(json, "ingredients"))

            return if (input.isEmpty()) {
                throw JsonParseException("No ingredients for shapeless recipe")
            } else if (input.size > 9) {
                throw JsonParseException("Too many ingredients for shapeless recipe")
            } else {
                val output = RedstoneAssemblerShapedRecipe.outputFromJson(JsonHelper.getObject(json, "result"))

                val cookTime = JsonHelper.getInt(json, "cookingtime", 100)
                val energyPerTick = JsonHelper.getInt(json, "energypertick", 5)

                RedstoneAssemblerShapelessRecipe(id, group, output, input, cookTime, energyPerTick)
            }
        }

        private fun getIngredients(json: JsonArray): DefaultedList<Ingredient> {
            val defaultedList = DefaultedList.of<Ingredient>()

            for (i in 0 until json.size()) {
                val ingredient = Ingredient.fromJson(json[i])
                if (!ingredient.isEmpty) {
                    defaultedList.add(ingredient)
                }
            }

            return defaultedList
        }

        override fun read(id: Identifier, buf: PacketByteBuf): RedstoneAssemblerShapelessRecipe {
            val group = buf.readString()
            val cookTime = buf.readVarInt()
            val energyPerTick = buf.readVarInt()
            val ingredientCount = buf.readVarInt()
            val input = DefaultedList.ofSize(ingredientCount, Ingredient.EMPTY)

            for (j in input.indices) {
                input[j] = Ingredient.fromPacket(buf)
            }

            val output = buf.readItemStack()
            return RedstoneAssemblerShapelessRecipe(id, group, output, input, cookTime, energyPerTick)
        }

        override fun write(buf: PacketByteBuf, recipe: RedstoneAssemblerShapelessRecipe) {
            buf.writeString(recipe.group)
            buf.writeVarInt(recipe.cookTime)
            buf.writeVarInt(recipe.energyPerTick)
            buf.writeVarInt(recipe.input.size)

            for (ingredient in recipe.input) {
                ingredient.write(buf)
            }

            buf.writeItemStack(recipe.output)
        }
    }
}
