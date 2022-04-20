package com.kneelawk.wiredredstone.util

import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Direction.*

object RotationUtils {
    fun rotatedPosX(to: Direction, x: Float, y: Float): Float {
        return when (to) {
            DOWN -> x
            UP -> x
            NORTH -> x
            SOUTH -> 1.0f - x
            WEST -> y
            EAST -> 1.0f - y
        }
    }

    fun rotatedPosY(to: Direction, y: Float, z: Float): Float {
        return when (to) {
            DOWN -> y
            UP -> 1.0f - y
            NORTH -> 1.0f - z
            SOUTH -> 1.0f - z
            WEST -> 1.0f - z
            EAST -> 1.0f - z
        }
    }

    fun rotatedPosZ(to: Direction, x: Float, y: Float, z: Float): Float {
        return when (to) {
            DOWN -> z
            UP -> 1.0f - z
            NORTH -> y
            SOUTH -> 1.0f - y
            WEST -> 1.0f - x
            EAST -> x
        }
    }

    fun rotatedNormX(to: Direction, x: Float, y: Float): Float {
        return when (to) {
            DOWN -> x
            UP -> x
            NORTH -> x
            SOUTH -> -x
            WEST -> y
            EAST -> -y
        }
    }

    fun rotatedNormY(to: Direction, y: Float, z: Float): Float {
        return when (to) {
            DOWN -> y
            UP -> -y
            NORTH -> -z
            SOUTH -> -z
            WEST -> -z
            EAST -> -z
        }
    }

    fun rotatedNormZ(to: Direction, x: Float, y: Float, z: Float): Float {
        return when (to) {
            DOWN -> z
            UP -> -z
            NORTH -> y
            SOUTH -> -y
            WEST -> -x
            EAST -> x
        }
    }

    /**
     * Rotates a given direction so that if it had been pointing `DOWN` before, it now points in the direction of `to`.
     */
    fun rotatedDirection(to: Direction, direction: Direction): Direction {
        return when (to) {
            DOWN -> direction
            UP -> when (direction) {
                DOWN -> UP
                UP -> DOWN
                NORTH -> SOUTH
                SOUTH -> NORTH
                WEST -> WEST
                EAST -> EAST
            }
            NORTH -> when (direction) {
                DOWN -> NORTH
                UP -> SOUTH
                NORTH -> UP
                SOUTH -> DOWN
                WEST -> WEST
                EAST -> EAST
            }
            SOUTH -> when (direction) {
                DOWN -> SOUTH
                UP -> NORTH
                NORTH -> UP
                SOUTH -> DOWN
                WEST -> EAST
                EAST -> WEST
            }
            WEST -> when (direction) {
                DOWN -> WEST
                UP -> EAST
                NORTH -> UP
                SOUTH -> DOWN
                WEST -> SOUTH
                EAST -> NORTH
            }
            EAST -> when (direction) {
                DOWN -> EAST
                UP -> WEST
                NORTH -> UP
                SOUTH -> DOWN
                WEST -> NORTH
                EAST -> SOUTH
            }
        }
    }

    /**
     * Rotates a given direction so that if it were pointing in the `from` direction before, it now points `DOWN`.
     */
    fun unrotatedDirection(from: Direction, direction: Direction): Direction {
        return when (from) {
            DOWN -> direction
            UP -> when (direction) {
                DOWN -> UP
                UP -> DOWN
                NORTH -> SOUTH
                SOUTH -> NORTH
                WEST -> WEST
                EAST -> EAST
            }
            NORTH -> when (direction) {
                DOWN -> SOUTH
                UP -> NORTH
                NORTH -> DOWN
                SOUTH -> UP
                WEST -> WEST
                EAST -> EAST
            }
            SOUTH -> when (direction) {
                DOWN -> SOUTH
                UP -> NORTH
                NORTH -> UP
                SOUTH -> DOWN
                WEST -> EAST
                EAST -> WEST
            }
            WEST -> when (direction) {
                DOWN -> SOUTH
                UP -> NORTH
                NORTH -> EAST
                SOUTH -> WEST
                WEST -> DOWN
                EAST -> UP
            }
            EAST -> when (direction) {
                DOWN -> SOUTH
                UP -> NORTH
                NORTH -> WEST
                SOUTH -> EAST
                WEST -> UP
                EAST -> DOWN
            }
        }
    }

    fun rotatedBox(to: Direction, box: Box): Box {
        return when (to) {
            DOWN -> box
            UP -> Box(box.minX, 1.0 - box.minY, 1.0 - box.minZ, box.maxX, 1.0 - box.maxY, 1.0 - box.maxZ)
            NORTH -> Box(box.minX, 1.0 - box.minZ, box.minY, box.maxX, 1.0 - box.maxZ, box.maxY)
            SOUTH -> Box(1.0 - box.minX, 1.0 - box.minZ, 1.0 - box.minY, 1.0 - box.maxX, 1.0 - box.maxZ, 1.0 - box.maxY)
            WEST -> Box(box.minY, 1.0 - box.minZ, 1.0 - box.minX, box.maxY, 1.0 - box.maxZ, 1.0 - box.maxX)
            EAST -> Box(1.0 - box.minY, 1.0 - box.minZ, box.minX, 1.0 - box.maxY, 1.0 - box.maxZ, box.maxX)
        }
    }

    /**
     * Rotates X values from NORTH to the given direction.
     */
    fun cardinalRotatedPosX(to: Direction, x: Float, z: Float): Float {
        return when (to) {
            NORTH -> x
            SOUTH -> 1f - x
            WEST -> z
            EAST -> 1f - z
            else -> throw IllegalArgumentException("$to is not a cardinal direction")
        }
    }

    /**
     * Rotates Z values from NORTH to the given direction.
     */
    fun cardinalRotatedPosZ(to: Direction, x: Float, z: Float): Float {
        return when (to) {
            NORTH -> z
            SOUTH -> 1f - z
            WEST -> 1f - x
            EAST -> x
            else -> throw IllegalArgumentException("$to is not a cardinal direction")
        }
    }

    /**
     * Gets the rotated normal X value for a rotation from NORTH to the given direction.
     */
    fun cardinalRotatedNormX(to: Direction, x: Float, z: Float): Float {
        return when (to) {
            NORTH -> x
            SOUTH -> -x
            WEST -> z
            EAST -> -z
            else -> throw IllegalArgumentException("$to is not a cardinal direction")
        }
    }

    /**
     * Gets the rotated normal Z value for a rotation from NORTH to the given direction.
     */
    fun cardinalRotatedNormZ(to: Direction, x: Float, z: Float): Float {
        return when (to) {
            NORTH -> z
            SOUTH -> -z
            WEST -> -x
            EAST -> x
            else -> throw IllegalArgumentException("$to is not a cardinal direction")
        }
    }

    /**
     * Rotates a direction around the Y-axis so that if it had been pointing NORTH, it now points in the given direction.
     */
    fun cardinalRotatedDirection(to: Direction, direction: Direction): Direction {
        return when (to) {
            NORTH -> direction
            SOUTH -> when (direction) {
                DOWN -> DOWN
                UP -> UP
                NORTH -> SOUTH
                SOUTH -> NORTH
                WEST -> EAST
                EAST -> WEST
            }
            WEST -> when (direction) {
                DOWN -> DOWN
                UP -> UP
                NORTH -> WEST
                SOUTH -> EAST
                WEST -> SOUTH
                EAST -> NORTH
            }
            EAST -> when (direction) {
                DOWN -> DOWN
                UP -> UP
                NORTH -> EAST
                SOUTH -> WEST
                WEST -> NORTH
                EAST -> SOUTH
            }
            else -> throw IllegalArgumentException("$to is not a cardinal direction")
        }
    }

    /**
     * Rotates a box from NORTH to the given cardinal direction.
     */
    fun cardinalRotatedBox(to: Direction, box: Box): Box {
        return when (to) {
            NORTH -> box
            SOUTH -> Box(1.0 - box.minX, box.minY, 1.0 - box.minZ, 1.0 - box.maxX, box.maxY, 1.0 - box.maxZ)
            WEST -> Box(box.minZ, box.minY, 1.0 - box.minX, box.maxZ, box.maxY, 1.0 - box.maxX)
            EAST -> Box(1.0 - box.minZ, box.minY, box.minX, 1.0 - box.maxZ, box.maxY, box.maxX)
            else -> throw IllegalArgumentException("$to is not a cardinal direction")
        }
    }

    /**
     * Gets the index of this cardinal direction with respective to the NORTH = no rotation convention.
     */
    fun cardinalRotatedIndex(dir: Direction): Int {
        return (dir.horizontal + 2) % 4
    }
}