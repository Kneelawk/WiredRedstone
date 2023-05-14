package com.kneelawk.wiredredstone.logic

import com.kneelawk.graphlib.api.util.SidedPos
import net.minecraft.world.World

/**
 * Used to allow bundled cables to know what blocks to connect to and send updates to.
 *
 * If a block is not indicated as having a bundled connection, it will not receive block updates from bundled cables.
 */
fun interface BundledConnectionFinder {
    /**
     * Checks whether a block should connect to bundled cables.
     *
     * @param world the world within which bundled connectivity is being tested.
     * @param pos the block-position and block-side where bundled connectivity is being tested.
     * @return whether the tested block should connect to bundled cables.
     */
    fun hasBundledConnection(world: World, pos: SidedPos): Boolean
}
