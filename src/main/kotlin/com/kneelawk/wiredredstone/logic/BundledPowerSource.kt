package com.kneelawk.wiredredstone.logic

import com.kneelawk.graphlib.api.v1.util.SidedPos
import net.minecraft.server.world.ServerWorld

/**
 * Used to provide bundled power to bundled cables.
 */
fun interface BundledPowerSource {
    /**
     * Gets the power provided at a specific location.
     *
     * @param world the world within which bundled power is being tested.
     * @param pos block-position and block-side where bundled power is being tested.
     * @return an unsigned 64-bit integer representing the analog (0-15) states of each wire within the bundle.
     */
    fun getBundledPower(world: ServerWorld, pos: SidedPos): ULong
}
