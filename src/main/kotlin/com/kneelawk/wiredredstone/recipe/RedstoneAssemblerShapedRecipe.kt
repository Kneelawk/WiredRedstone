package com.kneelawk.wiredredstone.recipe

import com.google.common.collect.Maps
import com.google.common.collect.Sets
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonSyntaxException
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.network.PacketByteBuf
import net.minecraft.recipe.Ingredient
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.util.Identifier
import net.minecraft.util.JsonHelper
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.registry.Registry
import net.minecraft.world.World
import kotlin.math.max
import kotlin.math.min

class RedstoneAssemblerShapedRecipe(
    private val id: Identifier, private val group: String, val width: Int, val height: Int,
    private val input: DefaultedList<Ingredient>, private val output: ItemStack, override val energyPerTick: Int,
    override val cookTime: Int
) : RedstoneAssemblerRecipe {

    companion object {
        /**
         * Compiles a pattern and series of symbols into a list of ingredients (the matrix) suitable for matching
         * against a crafting grid.
         */
        private fun createPatternMatrix(
            pattern: Array<String>, symbols: Map<String, Ingredient>, width: Int, height: Int
        ): DefaultedList<Ingredient> {
            val defaultedList = DefaultedList.ofSize(width * height, Ingredient.EMPTY)
            val set: MutableSet<String> = Sets.newHashSet(symbols.keys)
            set.remove(" ")

            for (i in pattern.indices) {
                for (j in 0 until pattern[i].length) {
                    val string = pattern[i].substring(j, j + 1)
                    val ingredient = symbols[string]
                        ?: throw JsonSyntaxException(
                            "Pattern references symbol '$string' but it's not defined in the key"
                        )
                    set.remove(string)
                    defaultedList[j + width * i] = ingredient
                }
            }
            if (set.isNotEmpty()) {
                throw JsonSyntaxException("Key defines symbols that aren't used in pattern: $set")
            } else {
                return defaultedList
            }
        }

        /**
         * Removes empty space from around the recipe pattern.
         *
         * Turn patterns such as:
         * ```
         * "   o"
         * "   a"
         * "    "
         * ```
         *
         * Into:
         * ```
         * "o"
         * "a"
         *```
         *
         * @return a new recipe pattern with all leading and trailing empty rows/columns removed
         */
        private fun removePadding(vararg pattern: String): Array<String> {
            var firstSymbol = Int.MAX_VALUE
            var lastSymbol = 0
            var fromTop = 0
            var fromBottom = 0

            for (i in pattern.indices) {
                val string = pattern[i]

                firstSymbol = min(firstSymbol, findFirstSymbol(string))
                val last = findLastSymbol(string)
                lastSymbol = max(lastSymbol, last)

                if (last < 0) {
                    if (fromTop == i) {
                        ++fromTop
                    }
                    ++fromBottom
                } else {
                    fromBottom = 0
                }
            }

            return if (pattern.size == fromBottom) {
                Array(0) { "" }
            } else {
                Array(pattern.size - fromBottom - fromTop) { i ->
                    pattern[i + fromTop].substring(firstSymbol, lastSymbol + 1)
                }
            }
        }

        private fun findFirstSymbol(line: String): Int {
            var i = 0
            while (i < line.length && line[i] == ' ') {
                ++i
            }
            return i
        }

        private fun findLastSymbol(pattern: String): Int {
            var i = pattern.length - 1
            while (i >= 0 && pattern[i] == ' ') {
                --i
            }
            return i
        }

        private fun getPattern(json: JsonArray): Array<String> {
            val strings = arrayOfNulls<String>(json.size())

            return if (strings.size > 3) {
                throw JsonSyntaxException("Invalid pattern: too many rows, 3 is maximum")
            } else if (strings.isEmpty()) {
                throw JsonSyntaxException("Invalid pattern: empty pattern not allowed")
            } else {
                for (i in strings.indices) {
                    val string = JsonHelper.asString(json[i], "pattern[$i]")

                    if (string.length > 3) {
                        throw JsonSyntaxException("Invalid pattern: too many columns, 3 is maximum")
                    }

                    if (i > 0 && strings[0]!!.length != string.length) {
                        throw JsonSyntaxException("Invalid pattern: each row must be the same width")
                    }

                    strings[i] = string
                }

                strings.requireNoNulls()
            }
        }

        /**
         * Reads the pattern symbols.
         *
         * @return a mapping from a symbol to the ingredient it represents
         */
        private fun readSymbols(json: JsonObject): Map<String, Ingredient> {
            val map: MutableMap<String, Ingredient> = Maps.newHashMap()
            for ((key, value) in json.entrySet()) {
                if ((key as String).length != 1) {
                    throw JsonSyntaxException(
                        "Invalid key entry: '$key' is an invalid symbol (must be 1 character only)."
                    )
                }
                if (" " == key) {
                    throw JsonSyntaxException("Invalid key entry: ' ' is a reserved symbol.")
                }
                map[key] = Ingredient.fromJson(value)
            }
            map[" "] = Ingredient.EMPTY
            return map
        }

        fun outputFromJson(json: JsonObject): ItemStack {
            val item = getItem(json)
            return if (json.has("data")) {
                throw JsonParseException("Disallowed data tag found")
            } else {
                val i = JsonHelper.getInt(json, "count", 1)
                if (i < 1) {
                    throw JsonSyntaxException("Invalid output count: $i")
                } else {
                    ItemStack(item, i)
                }
            }
        }

        private fun getItem(json: JsonObject): Item {
            val string = JsonHelper.getString(json, "item")
            val item = Registry.ITEM.getOrEmpty(Identifier(string)).orElseThrow {
                JsonSyntaxException(
                    "Unknown item '$string'"
                )
            } as Item
            return if (item === Items.AIR) {
                throw JsonSyntaxException("Invalid item: $string")
            } else {
                item
            }
        }
    }

    override fun matches(inventory: RedstoneAssemblerInventory, world: World): Boolean {
        for (i in 0..inventory.width - width) {
            for (j in 0..inventory.height - height) {
                if (matchesPattern(inventory, i, j, true)) {
                    return true
                }
                if (matchesPattern(inventory, i, j, false)) {
                    return true
                }
            }
        }

        return false
    }

    private fun matchesPattern(inv: RedstoneAssemblerInventory, offsetX: Int, offsetY: Int, flipped: Boolean): Boolean {
        for (invX in 0 until inv.width) {
            for (invY in 0 until inv.height) {
                val inputX = invX - offsetX
                val inputY = invY - offsetY

                var ingredient = Ingredient.EMPTY
                if (inputX >= 0 && inputY >= 0 && inputX < width && inputY < height) {
                    ingredient = if (flipped) {
                        input[width - inputX - 1 + inputY * width]
                    } else {
                        input[inputX + inputY * width]
                    }
                }

                if (!ingredient.test(inv.getCraftingStack(invX, invY))) {
                    return false
                }
            }
        }
        return true
    }

    override fun craft(inventory: RedstoneAssemblerInventory): ItemStack = output.copy()

    override fun fits(width: Int, height: Int): Boolean = width <= this.width && height <= this.height

    override fun getOutput(): ItemStack = output

    override fun getIngredients(): DefaultedList<Ingredient> = input

    override fun isEmpty(): Boolean {
        val defaultedList = this.ingredients
        return (defaultedList.isEmpty() || defaultedList.stream()
            .filter { ingredient: Ingredient -> !ingredient.isEmpty }
            .anyMatch { ingredient: Ingredient -> ingredient.matchingStacks.isEmpty() })
    }

    override fun getId(): Identifier = id

    override fun getGroup(): String = group

    override fun getSerializer(): RecipeSerializer<*> = Serializer

    object Serializer : RecipeSerializer<RedstoneAssemblerShapedRecipe> {
        override fun read(id: Identifier, json: JsonObject): RedstoneAssemblerShapedRecipe {
            val group = JsonHelper.getString(json, "group", "")!!

            val keys = readSymbols(JsonHelper.getObject(json, "key"))
            val pattern = removePadding(*getPattern(JsonHelper.getArray(json, "pattern")))

            val width = pattern[0].length
            val height = pattern.size

            val input = createPatternMatrix(pattern, keys, width, height)

            val output = outputFromJson(JsonHelper.getObject(json, "result"))

            val cookTime = JsonHelper.getInt(json, "cookingtime", 100)
            val energyPerTick = JsonHelper.getInt(json, "energypertick", 5)

            return RedstoneAssemblerShapedRecipe(id, group, width, height, input, output, energyPerTick, cookTime)
        }

        override fun read(id: Identifier, buf: PacketByteBuf): RedstoneAssemblerShapedRecipe {
            val width = buf.readVarInt()
            val height = buf.readVarInt()
            val energyPerTick = buf.readVarInt()
            val cookTime = buf.readVarInt()
            val group = buf.readString()
            val input = DefaultedList.ofSize(width * height, Ingredient.EMPTY)

            for (k in input.indices) {
                input[k] = Ingredient.fromPacket(buf)
            }

            val output = buf.readItemStack()
            return RedstoneAssemblerShapedRecipe(id, group, width, height, input, output, energyPerTick, cookTime)
        }

        override fun write(buf: PacketByteBuf, recipe: RedstoneAssemblerShapedRecipe) {
            buf.writeVarInt(recipe.width)
            buf.writeVarInt(recipe.height)
            buf.writeVarInt(recipe.energyPerTick)
            buf.writeVarInt(recipe.cookTime)
            buf.writeString(recipe.group)

            for (ingredient in recipe.input) {
                ingredient.write(buf)
            }

            buf.writeItemStack(recipe.output)
        }
    }
}
