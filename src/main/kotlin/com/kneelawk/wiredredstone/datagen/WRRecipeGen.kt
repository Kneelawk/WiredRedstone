package com.kneelawk.wiredredstone.datagen

import com.kneelawk.wiredredstone.datagen.recipe.ShapedRARecipeJsonFactory
import com.kneelawk.wiredredstone.datagen.recipe.ShapelessRARecipeJsonFactory
import com.kneelawk.wiredredstone.item.WRItems
import com.kneelawk.wiredredstone.util.DyeColorUtil
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider
import net.minecraft.data.server.recipe.RecipeJsonProvider
import net.minecraft.item.Item
import net.minecraft.item.ItemConvertible
import net.minecraft.item.Items
import net.minecraft.recipe.RecipeCategory
import net.minecraft.registry.Registries
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.tag.TagKey
import net.minecraft.util.DyeColor
import net.minecraft.util.Identifier
import java.util.function.Consumer

class WRRecipeGen(output: FabricDataOutput) : FabricRecipeProvider(output) {
    override fun generateRecipes(exporter: Consumer<RecipeJsonProvider>) {
        val plainBundledCable = WRItems.BUNDLED_CABLES[null]!!
        val plainStandingBundledCable = WRItems.STANDING_BUNDLED_CABLES[null]!!

        standingConversion(exporter, "standing_red_alloy_wire", WRItems.STANDING_RED_ALLOY_WIRE, WRItems.RED_ALLOY_WIRE)
        standingConversion(exporter, "standing_bundled_cable", plainStandingBundledCable, plainBundledCable)

        ShapedRARecipeJsonFactory.create(RecipeCategory.REDSTONE, plainStandingBundledCable)
            .cookTime(50)
            .ingredient('W', WRTagGen.STANDING_INSULATED_WIRES)
            .ingredient('S', TagKey.of(RegistryKeys.ITEM, Identifier("c", "string")))
            .pattern("SWS")
            .pattern("WWW")
            .pattern("SWS")
            .group("standing_bundled_cable")
            .criterion(hasItem(WRItems.REDSTONE_ALLOY_INGOT), conditionsFromItem(WRItems.REDSTONE_ALLOY_INGOT))
            .offerTo(exporter)

        for (color in DyeColor.values()) {
            val dyeTag = WRTagGen.DYE_TAGS[color]!!
            val bundledCable = WRItems.BUNDLED_CABLES[color]!!
            val insulatedWire = WRItems.INSULATED_WIRES[color]!!
            val insulatedWireId = Registries.ITEM.getId(insulatedWire)
            val standingBundledCable = WRItems.STANDING_BUNDLED_CABLES[color]!!
            val standingInsulatedWire = WRItems.STANDING_INSULATED_WIRES[color]!!

            ShapelessRARecipeJsonFactory.create(RecipeCategory.REDSTONE, bundledCable)
                .cookTime(20)
                .ingredient(plainBundledCable)
                .ingredient(Items.PAPER)
                .ingredient(dyeTag)
                .group("bundled_cable")
                .criterion(hasItem(plainBundledCable), conditionsFromItem(plainBundledCable))
                .offerTo(exporter)

            recolor(exporter, "bundled_cable", bundledCable, dyeTag, WRTagGen.COLORED_BUNDLED_CABLES, plainBundledCable)

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

            recolor(
                exporter, "insulated_wire", insulatedWire, dyeTag, WRTagGen.INSULATED_WIRES,
                WRItems.REDSTONE_ALLOY_INGOT
            )

            ShapelessRARecipeJsonFactory.create(RecipeCategory.REDSTONE, insulatedWire, 2)
                .ingredient(WRItems.RED_ALLOY_WIRE)
                .ingredient(WRItems.RED_ALLOY_WIRE)
                .ingredient(DyeColorUtil.wool(color))
                .group("insulated_wire")
                .criterion(hasItem(WRItems.RED_ALLOY_WIRE), conditionsFromItem(WRItems.RED_ALLOY_WIRE))
                .offerTo(exporter, insulatedWireId.extendPath("_wrapping"))

            standingConversion(exporter, "standing_bundled_cable", standingBundledCable, bundledCable)
            standingConversion(exporter, "standing_insulated_wire", standingInsulatedWire, insulatedWire)

            recolor(
                exporter, "standing_bundled_cable", standingBundledCable, dyeTag,
                WRTagGen.COLORED_STANDING_BUNDLED_CABLES, plainBundledCable
            )
            recolor(
                exporter, "standing_insulated_wire", standingInsulatedWire, dyeTag, WRTagGen.STANDING_INSULATED_WIRES,
                WRItems.REDSTONE_ALLOY_INGOT
            )

            ShapelessRARecipeJsonFactory.create(RecipeCategory.REDSTONE, standingBundledCable)
                .cookTime(20)
                .ingredient(plainStandingBundledCable)
                .ingredient(Items.PAPER)
                .ingredient(dyeTag)
                .group("standing_bundled_cable")
                .criterion(hasItem(plainStandingBundledCable), conditionsFromItem(plainStandingBundledCable))
                .offerTo(exporter)
        }
    }

    fun standingConversion(
        exporter: Consumer<RecipeJsonProvider>, group: String, output: ItemConvertible, input: ItemConvertible
    ) {
        ShapedRARecipeJsonFactory.create(RecipeCategory.REDSTONE, output, 4)
            .cookTime(40)
            .ingredient('W', input)
            .ingredient('I', TagKey.of(RegistryKeys.ITEM, Identifier("c", "iron_ingots")))
            .pattern(" W ")
            .pattern("WIW")
            .pattern(" W ")
            .group(group)
            .criterion(hasItem(input), conditionsFromItem(input))
            .offerTo(exporter, Registries.ITEM.getId(output.asItem()).extendPath("_standing_conversion"))
    }

    fun recolor(
        exporter: Consumer<RecipeJsonProvider>, group: String, output: ItemConvertible, dyeTag: TagKey<Item>,
        inputTag: TagKey<Item>, criterion: ItemConvertible
    ) {
        ShapelessRARecipeJsonFactory.create(RecipeCategory.REDSTONE, output)
            .ingredient(inputTag)
            .ingredient(dyeTag)
            .group(group)
            .criterion(hasItem(criterion), conditionsFromItem(criterion))
            .offerTo(exporter, Registries.ITEM.getId(output.asItem()).extendPath("_recolor"))
    }
}
