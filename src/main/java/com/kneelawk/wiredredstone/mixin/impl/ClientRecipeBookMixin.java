package com.kneelawk.wiredredstone.mixin.impl;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.recipe_book.ClientRecipeBook;
import net.minecraft.client.recipe_book.RecipeBookGroup;
import net.minecraft.recipe.Recipe;

import com.kneelawk.wiredredstone.recipe.RedstoneAssemblerRecipeType;

/**
 * Mixin to remove the warnings when the Minecraft client loads the Redstone Assembler recipes.
 */
@Mixin(ClientRecipeBook.class)
public class ClientRecipeBookMixin {
    @Inject(method = "getGroupForRecipe", at = @At("HEAD"), cancellable = true)
    private static void onGetGroupForRecipe(Recipe<?> recipe, CallbackInfoReturnable<RecipeBookGroup> cir) {
        if (recipe.getType() == RedstoneAssemblerRecipeType.INSTANCE) {
            cir.setReturnValue(RecipeBookGroup.UNKNOWN);
        }
    }
}
