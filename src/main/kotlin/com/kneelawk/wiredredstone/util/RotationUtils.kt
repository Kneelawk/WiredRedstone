package com.kneelawk.wiredredstone.util

import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Direction.*

object RotationUtils {
    fun rotatedX(to: Direction, x: Float, y: Float): Float {
        return when (to) {
            DOWN -> x
            UP -> x
            NORTH -> x
            SOUTH -> 1.0f - x
            WEST -> y
            EAST -> 1.0f - y
        }
    }

    fun rotatedY(to: Direction, y: Float, z: Float): Float {
        return when (to) {
            DOWN -> y
            UP -> 1.0f - y
            NORTH -> 1.0f - z
            SOUTH -> 1.0f - z
            WEST -> 1.0f - z
            EAST -> 1.0f - z
        }
    }

    fun rotatedZ(to: Direction, x: Float, y: Float, z: Float): Float {
        return when (to) {
            DOWN -> z
            UP -> 1.0f - z
            NORTH -> y
            SOUTH -> 1.0f - y
            WEST -> 1.0f - x
            EAST -> x
        }
    }

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
}