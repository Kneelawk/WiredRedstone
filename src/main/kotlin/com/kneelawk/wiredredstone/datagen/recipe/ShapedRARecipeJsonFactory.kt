package com.kneelawk.wiredredstone.datagen.recipe

import com.google.gson.JsonObject
import com.kneelawk.wiredredstone.recipe.RedstoneAssemblerShapedRecipe
import net.minecraft.advancement.Advancement
import net.minecraft.advancement.AdvancementRewards
import net.minecraft.advancement.CriterionMerger
import net.minecraft.advancement.criterion.RecipeUnlockedCriterion
import net.minecraft.data.server.recipe.RecipeJsonProvider
import net.minecraft.data.server.recipe.ShapedRecipeJsonFactory
import net.minecraft.item.Item
import net.minecraft.item.ItemConvertible
import net.minecraft.recipe.CraftingCategory
import net.minecraft.recipe.Ingredient
import net.minecraft.recipe.RecipeCategory
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.util.Identifier
import java.util.function.Consumer

class ShapedRARecipeJsonFactory(category: RecipeCategory, result: ItemConvertible, resultCount: Int) :
    ShapedRecipeJsonFactory(category, result, resultCount) {
    companion object {
        fun create(category: RecipeCategory, result: ItemConvertible, resultCount: Int) =
            ShapedRARecipeJsonFactory(category, result, resultCount)

        fun create(category: RecipeCategory, result: ItemConvertible) = ShapedRARecipeJsonFactory(category, result, 1)
    }

    var cookTime = 10
    var energyPerTick = 5

    fun cookTime(cookTime: Int): ShapedRARecipeJsonFactory {
        this.cookTime = cookTime
        return this
    }

    fun energyPerTick(energyPerTick: Int): ShapedRARecipeJsonFactory {
        this.energyPerTick = energyPerTick
        return this
    }

    override fun offerTo(exporter: Consumer<RecipeJsonProvider>, recipeId: Identifier) {
        validate(recipeId)
        builder
            .parent(ROOT_ID)
            .criterion("has_the_recipe", RecipeUnlockedCriterion.create(recipeId))
            .rewards(AdvancementRewards.Builder.recipe(recipeId))
            .criteriaMerger(CriterionMerger.OR)
        exporter.accept(
            ShapedRARecipeJsonProvider(
                recipeId,
                result,
                resultCount,
                group ?: "",
                getCraftingCategory(this.category),
                pattern,
                ingredients,
                cookTime,
                energyPerTick,
                builder,
                recipeId.withPrefix("recipes/" + this.category.getName() + "/"),
                showNotification
            )
        )
    }

    class ShapedRARecipeJsonProvider(
        recipeId: Identifier, result: Item, resultCount: Int, group: String, category: CraftingCategory,
        pattern: MutableList<String>, ingredients: MutableMap<Char, Ingredient>, private val cookTime: Int,
        private val energyPerTick: Int, builder: Advancement.Task, advancementId: Identifier, showNotification: Boolean
    ) : ShapedRecipeJsonProvider(
        recipeId, result, resultCount, group, category, pattern, ingredients, builder, advancementId, showNotification
    ) {
        override fun getSerializer(): RecipeSerializer<*> = RedstoneAssemblerShapedRecipe.Serializer

        override fun serialize(json: JsonObject) {
            super.serialize(json)

            json.addProperty("cookingtime", cookTime)
            json.addProperty("energypertick", energyPerTick)
        }
    }
}
