package com.kneelawk.wiredredstone.datagen

import com.kneelawk.wiredredstone.datagen.recipe.ShapedRARecipeJsonFactory
import com.kneelawk.wiredredstone.datagen.recipe.ShapelessRARecipeJsonFactory
import com.kneelawk.wiredredstone.item.WRItems
import com.kneelawk.wiredredstone.util.DyeColorUtil
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider
import net.minecraft.data.server.recipe.RecipeJsonProvider
import net.minecraft.item.Items
import net.minecraft.recipe.RecipeCategory
import net.minecraft.registry.Registries
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.tag.TagKey
import net.minecraft.util.DyeColor
import net.minecraft.util.Identifier
import java.util.function.Consumer

class WRRecipeGen(output: FabricDataOutput) : FabricRecipeProvider(output) {
    override fun generateRecipes(exporter: Consumer<RecipeJsonProvider>?) {
        val plainBundledCable = WRItems.BUNDLED_CABLES[null]!!
        for (color in DyeColor.values()) {
            val dyeTag = WRTagGen.DYE_TAGS[color]!!
            val bundledCable = WRItems.BUNDLED_CABLES[color]!!
            val insulatedWire = WRItems.INSULATED_WIRES[color]!!
            val insulatedWireId = Registries.ITEM.getId(insulatedWire)

            ShapelessRARecipeJsonFactory.create(RecipeCategory.REDSTONE, bundledCable)
                .cookTime(20)
                .ingredient(plainBundledCable)
                .ingredient(Items.PAPER)
                .ingredient(dyeTag)
                .group("bundled_cable")
                .criterion(hasItem(plainBundledCable), conditionsFromItem(plainBundledCable))
                .offerTo(exporter)

            ShapelessRARecipeJsonFactory.create(RecipeCategory.REDSTONE, bundledCable)
                .ingredient(WRTagGen.COLORED_BUNDLED_CABLES)
                .ingredient(dyeTag)
                .group("bundled_cable")
                .criterion(hasItem(plainBundledCable), conditionsFromItem(plainBundledCable))
                .offerTo(exporter, Registries.ITEM.getId(bundledCable).extendPath("_recolor"))

            ShapedRARecipeJsonFactory.create(RecipeCategory.REDSTONE, insulatedWire, 12)
                .cookTime(100)
                .ingredient('W', DyeColorUtil.wool(color))
                .ingredient('I', TagKey.of(RegistryKeys.ITEM, Identifier("c", "red_alloy_ingots")))
                .pattern("WIW")
                .pattern("WIW")
                .pattern("WIW")
                .group("insulated_wire")
                .criterion(hasItem(WRItems.REDSTONE_ALLOY_INGOT), conditionsFromItem(WRItems.REDSTONE_ALLOY_INGOT))
                .offerTo(exporter)

            ShapelessRARecipeJsonFactory.create(RecipeCategory.REDSTONE, insulatedWire)
                .ingredient(WRTagGen.INSULATED_WIRES)
                .ingredient(dyeTag)
                .group("insulated_wire")
                .criterion(hasItem(WRItems.REDSTONE_ALLOY_INGOT), conditionsFromItem(WRItems.REDSTONE_ALLOY_INGOT))
                .offerTo(exporter, insulatedWireId.extendPath("_recolor"))

            ShapelessRARecipeJsonFactory.create(RecipeCategory.REDSTONE, insulatedWire, 2)
                .ingredient(WRItems.RED_ALLOY_WIRE)
                .ingredient(WRItems.RED_ALLOY_WIRE)
                .ingredient(DyeColorUtil.wool(color))
                .group("insulated_wire")
                .criterion(hasItem(WRItems.RED_ALLOY_WIRE), conditionsFromItem(WRItems.RED_ALLOY_WIRE))
                .offerTo(exporter, insulatedWireId.extendPath("_wrapping"))
        }
    }
}
