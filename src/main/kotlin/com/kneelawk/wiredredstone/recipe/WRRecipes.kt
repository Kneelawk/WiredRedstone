package com.kneelawk.wiredredstone.recipe

import com.kneelawk.wiredredstone.WRConstants.id
import net.minecraft.util.registry.Registry

object WRRecipes {
    val REDSTONE_ASSEMBLER_SHAPED = id("redstone_assembler_shaped")
    val REDSTONE_ASSEMBLER_SHAPELESS = id("redstone_assembler_shapeless")

    fun init() {
        Registry.register(Registry.RECIPE_TYPE, RedstoneAssemblerRecipeType.ID, RedstoneAssemblerRecipeType)
        Registry.register(
            Registry.RECIPE_SERIALIZER, REDSTONE_ASSEMBLER_SHAPED, RedstoneAssemblerShapedRecipe.Serializer
        )
        Registry.register(
            Registry.RECIPE_SERIALIZER, REDSTONE_ASSEMBLER_SHAPELESS, RedstoneAssemblerShapelessRecipe.Serializer
        )
    }
}
