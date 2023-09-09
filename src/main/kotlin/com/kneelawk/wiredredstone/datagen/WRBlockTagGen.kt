package com.kneelawk.wiredredstone.datagen

import com.kneelawk.wiredredstone.tag.WRBlockTags
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider.BlockTagProvider
import net.minecraft.block.Blocks
import net.minecraft.registry.HolderLookup
import java.util.concurrent.CompletableFuture

class WRBlockTagGen(output: FabricDataOutput, registries: CompletableFuture<HolderLookup.Provider>) :
    BlockTagProvider(output, registries) {
    override fun configure(arg: HolderLookup.Provider) {
        getOrCreateTagBuilder(WRBlockTags.WIRE_GATE_CONNECTABLE).add(Blocks.REPEATER, Blocks.COMPARATOR)
    }
}
