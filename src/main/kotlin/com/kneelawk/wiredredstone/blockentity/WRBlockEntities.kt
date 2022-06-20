package com.kneelawk.wiredredstone.blockentity

import com.kneelawk.wiredredstone.WRConstants.id
import com.kneelawk.wiredredstone.block.WRBlocks
import net.fabricmc.fabric.api.`object`.builder.v1.block.entity.FabricBlockEntityTypeBuilder
import net.minecraft.util.registry.Registry
import team.reborn.energy.api.EnergyStorage

object WRBlockEntities {
    val REDSTONE_ASSEMBLER by lazy {
        FabricBlockEntityTypeBuilder.create(::RedstoneAssemblerBlockEntity, WRBlocks.REDSTONE_ASSEMBLER).build()
    }

    fun init() {
        Registry.register(Registry.BLOCK_ENTITY_TYPE, id("redstone_assembler"), REDSTONE_ASSEMBLER)
        EnergyStorage.SIDED.registerForBlockEntity({ be, _ -> be.energyStorage }, REDSTONE_ASSEMBLER)
    }
}
