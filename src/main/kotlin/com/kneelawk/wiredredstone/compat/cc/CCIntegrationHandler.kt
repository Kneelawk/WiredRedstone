package com.kneelawk.wiredredstone.compat.cc

import com.kneelawk.graphlib.api.v1.util.SidedPos
import com.kneelawk.wiredredstone.WRLog
import com.kneelawk.wiredredstone.util.ReflectionUtils
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

object CCIntegrationHandler {
    private var integration: CCIntegration? = null

    fun init() {
        if (FabricLoader.getInstance().isModLoaded("computercraft")) {
            try {
                integration =
                    ReflectionUtils.loadObject<CCIntegration>(
                        "com.kneelawk.wiredredstone.compat.cc.impl.CCIntegrationImpl"
                    )
            } catch (ex: ClassNotFoundException) {
                WRLog.warn(
                    "Attempted to load ComputerCraft integration, but found that this version of Wired Redstone is " +
                            "not compiled with ComputerCraft integration. ComputerCraft integration will not work."
                )
            }
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
