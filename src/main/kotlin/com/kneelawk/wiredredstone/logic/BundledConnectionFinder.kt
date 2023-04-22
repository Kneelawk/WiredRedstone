package com.kneelawk.wiredredstone.logic

import com.kneelawk.graphlib.util.SidedPos
import net.minecraft.world.World

fun interface BundledConnectionFinder {
    fun hasBundledConnection(world: World, pos: SidedPos): Boolean
}
