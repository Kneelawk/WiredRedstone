package com.kneelawk.wiredredstone.datagen

import alexiil.mc.lib.multipart.api.PartLootParams
import com.kneelawk.wiredredstone.item.WRItems
import com.kneelawk.wiredredstone.part.WRParts
import com.kneelawk.wiredredstone.util.DyeColorUtil
import com.kneelawk.wiredredstone.util.LootTableUtil
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.SimpleFabricLootTableProvider
import net.minecraft.item.ItemConvertible
import net.minecraft.loot.LootPool
import net.minecraft.loot.LootTable
import net.minecraft.loot.condition.SurvivesExplosionLootCondition
import net.minecraft.loot.entry.ItemEntry
import net.minecraft.loot.provider.number.ConstantLootNumberProvider
import net.minecraft.util.DyeColor
import net.minecraft.util.Identifier
import java.util.function.BiConsumer

class WRPartLootTableGen(dataOutput: FabricDataOutput) :
    SimpleFabricLootTableProvider(dataOutput, PartLootParams.PART_TYPE) {
    companion object {
        private val REGULAR_PARTS = mapOf(
            WRParts.RED_ALLOY_WIRE to WRItems.RED_ALLOY_WIRE,
            WRParts.BUNDLED_CABLE to WRItems.BUNDLED_CABLE,
            WRParts.STANDING_RED_ALLOY_WIRE to WRItems.STANDING_RED_ALLOY_WIRE,
            WRParts.POWERLINE_CONNECTOR to WRItems.POWERLINE_CONNECTOR,
            WRParts.GATE_AND to WRItems.GATE_AND,
            WRParts.GATE_DIODE to WRItems.GATE_DIODE,
            WRParts.GATE_NAND to WRItems.GATE_NAND,
            WRParts.GATE_NOR to WRItems.GATE_NOR,
            WRParts.GATE_NOT to WRItems.GATE_NOT,
            WRParts.GATE_OR to WRItems.GATE_OR,
            WRParts.GATE_PROJECTOR_SIMPLE to WRItems.GATE_PROJECTOR_SIMPLE,
            WRParts.GATE_REPEATER to WRItems.GATE_REPEATER,
            WRParts.GATE_RS_LATCH to WRItems.GATE_RS_LATCH
        )
    }

    override fun generate(biConsumer: BiConsumer<Identifier, LootTable.Builder>) {
        for ((part, item) in REGULAR_PARTS) {
            biConsumer.accept(LootTableUtil.getLootTableId(part.identifier), singleDrop(item))
        }

        for (dyeColor in DyeColor.values()) {
            val insulatedId = WRParts.INSULATED_WIRE.identifier.withPrefix(dyeColor.getName() + "_")
            val bundledId = WRParts.BUNDLED_CABLE.identifier.withPrefix(dyeColor.getName() + "_")

            biConsumer.accept(
                LootTableUtil.getLootTableId(insulatedId), singleDrop(DyeColorUtil.insulatedWire(dyeColor))
            )
            biConsumer.accept(LootTableUtil.getLootTableId(bundledId), singleDrop(DyeColorUtil.bundledCable(dyeColor)))
        }
    }

    private fun singleDrop(drop: ItemConvertible): LootTable.Builder {
        return LootTable.builder().pool(
            LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0f)).with(ItemEntry.builder(drop))
                .conditionally(SurvivesExplosionLootCondition.builder())
        )
    }
}
