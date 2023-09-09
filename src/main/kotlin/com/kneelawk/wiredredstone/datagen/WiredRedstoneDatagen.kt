package com.kneelawk.wiredredstone.datagen

import com.kneelawk.wiredredstone.WRLog
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator

fun init(gen: FabricDataGenerator) {
    WRLog.log.info("[WiredRedstone] Starting data generation...")

    val pack = gen.createPack()
    pack.addProvider(::WRBlockTagGen)
    pack.addProvider(::WRItemTagGen)
    pack.addProvider(::WRModelGen)
    pack.addProvider(::WRPartLootTableGen)
    pack.addProvider(::WRRecipeGen)
}
