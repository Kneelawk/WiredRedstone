package com.kneelawk.wiredredstone.util

import net.minecraft.util.math.Direction

object DirectionUtils {
    val HORIZONTALS: Array<Direction> = Array(4) { Direction.fromHorizontal(it) }

    /**
     * Checks if the given direction is a valid cardinal direction (NORTH, SOUTH, WEST, EAST).
     */
    fun isValid(dir: Direction): Boolean {
        return dir.horizontal >= 0
    }
}