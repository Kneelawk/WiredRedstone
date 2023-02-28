package com.kneelawk.wiredredstone.recipe

import com.kneelawk.wiredredstone.WRConstants.id
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry

object WRRecipes {
    val REDSTONE_ASSEMBLER_SHAPED = id("redstone_assembler_shaped")
    val REDSTONE_ASSEMBLER_SHAPELESS = id("redstone_assembler_shapeless")

    fun init() {
        Registry.register(Registries.RECIPE_TYPE, RedstoneAssemblerRecipeType.ID, RedstoneAssemblerRecipeType)
        Registry.register(
            Registries.RECIPE_SERIALIZER, REDSTONE_ASSEMBLER_SHAPED, RedstoneAssemblerShapedRecipe.Serializer
        )
        Registry.register(
            Registries.RECIPE_SERIALIZER, REDSTONE_ASSEMBLER_SHAPELESS, RedstoneAssemblerShapelessRecipe.Serializer
        )
    }
}
