package com.kneelawk.wiredredstone.util.bits

import net.minecraft.util.math.Direction

/**
 * This object is like ConnectionUtils, except that it deals with blockage values instead of connection values.
 */
object BlockageUtils {
    const val UNBLOCKED: UByte = 0u

    /**
     * Gets the bitshift for the given direction of the four cardinal directions.
     */
    private fun shiftFor(dir: Direction): Int {
        return dir.horizontal
    }

    private fun maskFor(dir: Direction): UByte {
        return 1u.toUByte().rotateLeft(shiftFor(dir))
    }

    fun isBlocked(blockage: UByte, dir: Direction): Boolean {
        if (dir.horizontal < 0)
            throw IllegalArgumentException("$dir is not a valid cardinal direction")
        val mask = maskFor(dir)
        return blockage and mask == mask
    }

    fun setBlocked(blockage: UByte, dir: Direction): UByte {
        if (dir.horizontal < 0)
            throw IllegalArgumentException("$dir is not a valid cardinal direction")
        return blockage or maskFor(dir)
    }

    fun setUnblocked(blockage: UByte, dir: Direction): UByte {
        if (dir.horizontal < 0)
            throw IllegalArgumentException("$dir is not a valid cardinal direction")
        return blockage and maskFor(dir).inv()
    }
}
