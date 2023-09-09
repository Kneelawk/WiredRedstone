package com.kneelawk.wiredredstone.datagen.recipe

import com.google.gson.JsonObject
import com.kneelawk.wiredredstone.recipe.RedstoneAssemblerShapelessRecipe
import net.minecraft.advancement.Advancement
import net.minecraft.advancement.AdvancementRewards
import net.minecraft.advancement.CriterionMerger
import net.minecraft.advancement.criterion.RecipeUnlockedCriterion
import net.minecraft.data.server.recipe.RecipeJsonProvider
import net.minecraft.data.server.recipe.ShapelessRecipeJsonFactory
import net.minecraft.item.Item
import net.minecraft.item.ItemConvertible
import net.minecraft.recipe.CraftingCategory
import net.minecraft.recipe.Ingredient
import net.minecraft.recipe.RecipeCategory
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.util.Identifier
import java.util.function.Consumer

class ShapelessRARecipeJsonFactory(category: RecipeCategory, result: ItemConvertible, resultCount: Int) :
    ShapelessRecipeJsonFactory(category, result, resultCount) {
    companion object {
        fun create(category: RecipeCategory, result: ItemConvertible, resultCount: Int) =
            ShapelessRARecipeJsonFactory(category, result, resultCount)

        fun create(category: RecipeCategory, result: ItemConvertible) =
            ShapelessRARecipeJsonFactory(category, result, 1)
    }

    var cookTime = 10
    var energyPerTick = 5

    fun cookTime(cookTime: Int): ShapelessRARecipeJsonFactory {
        this.cookTime = cookTime
        return this
    }

    fun energyPerTick(energyPerTick: Int): ShapelessRARecipeJsonFactory {
        this.energyPerTick = energyPerTick
        return this
    }

    override fun offerTo(exporter: Consumer<RecipeJsonProvider>, recipeId: Identifier) {
        validate(recipeId)
        builder.parent(ROOT_ID)
            .criterion("has_the_recipe", RecipeUnlockedCriterion.create(recipeId))
            .rewards(AdvancementRewards.Builder.recipe(recipeId))
            .criteriaMerger(CriterionMerger.OR)
        exporter.accept(
            ShapelessRARecipeJsonProvider(
                recipeId,
                result,
                resultCount,
                group ?: "",
                getCraftingCategory(this.category),
                ingredients,
                cookTime,
                energyPerTick,
                builder,
                recipeId.withPrefix("recipes/" + this.category.getName() + "/")
            )
        )
    }

    class ShapelessRARecipeJsonProvider(
        recipeId: Identifier, result: Item, count: Int, group: String, category: CraftingCategory,
        ingredients: MutableList<Ingredient>, private val cookTime: Int, private val energyPerTick: Int,
        builder: Advancement.Task, advancementId: Identifier
    ) : ShapelessRecipeJsonProvider(recipeId, result, count, group, category, ingredients, builder, advancementId) {
        override fun getSerializer(): RecipeSerializer<*> = RedstoneAssemblerShapelessRecipe.Serializer

        override fun serialize(json: JsonObject) {
            super.serialize(json)

            json.addProperty("cookingtime", cookTime)
            json.addProperty("energypertick", energyPerTick)
        }
    }
}
