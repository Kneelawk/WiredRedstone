package com.kneelawk.wiredredstone.emi

import dev.emi.emi.api.recipe.EmiRecipe

interface RedstoneAssemblerEmiRecipe : EmiRecipe {
    val width: Int
    val height: Int
}
