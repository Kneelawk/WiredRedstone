package com.kneelawk.wiredredstone.compat.cc

import com.kneelawk.graphlib.util.SidedPos
import com.kneelawk.wiredredstone.util.ReflectionUtils
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

object CCIntegrationHandler {
    private var integration: CCIntegration? = null

    fun init() {
        if (FabricLoader.getInstance().isModLoaded("computercraft")) {
            integration =
                ReflectionUtils.loadObject<CCIntegration>("com.kneelawk.wiredredstone.compat.cc.CCIntegrationImpl")
            integration?.init()
        }
    }

    fun getBundledCableInput(world: ServerWorld, pos: SidedPos): UShort {
        return integration?.getBundledCableInput(world, pos) ?: 0u
    }

    fun hasBundledCableOutput(world: World, pos: BlockPos): Boolean {
        return integration?.hasBundledCableOutput(world, pos) ?: false
    }
}
