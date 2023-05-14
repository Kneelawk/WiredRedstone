package com.kneelawk.wiredredstone.compat.emi.impl

import dev.emi.emi.api.recipe.EmiRecipe

interface RedstoneAssemblerEmiRecipe : EmiRecipe {
    val width: Int
    val height: Int
}
