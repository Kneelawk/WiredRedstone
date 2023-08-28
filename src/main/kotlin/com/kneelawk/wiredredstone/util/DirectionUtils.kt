package com.kneelawk.wiredredstone.util

import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3i

object DirectionUtils {
    val HORIZONTALS: Array<Direction> = Array(4) { Direction.fromHorizontal(it) }

    /**
     * Checks if the given direction is a valid cardinal direction (NORTH, SOUTH, WEST, EAST).
     */
    fun isHorizontal(dir: Direction): Boolean {
        return dir.horizontal >= 0
    }

    /**
     * Checks if the given direction is horizontal, throwing an exception if not.
     */
    fun assertHorizontal(dir: Direction) {
        if (!isHorizontal(dir))
            throw IllegalArgumentException("$dir is not a horizontal direction")
    }

    /**
     * If the [dir] is horizontal, this returns [dir], otherwise, this returns [Direction.NORTH].
     */
    fun makeHorizontal(dir: Direction): Direction {
        return if (isHorizontal(dir)) dir else Direction.NORTH
    }

    fun fromVector(vec: Vec3i) = Direction.fromXYZ(vec.x, vec.y, vec.z)
}
