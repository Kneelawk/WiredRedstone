package com.kneelawk.wiredredstone.util.bits

import com.kneelawk.wiredredstone.util.RotationUtils
import net.minecraft.util.math.Direction

object ConnectionUtils {
    /**
     * Gets the bitshift for a given direction of the four cardinal directions.
     */
    private fun shiftFor(dir: Direction): Int {
        return dir.horizontal shl 1
    }

    private fun maskForInternal(dir: Direction): UByte {
        return 1u.toUByte().rotateLeft(shiftFor(dir))
    }

    private fun maskForExternal(dir: Direction): UByte {
        return 2u.toUByte().rotateLeft(shiftFor(dir))
    }

    private fun maskForCorner(dir: Direction): UByte {
        return 3u.toUByte().rotateLeft(shiftFor(dir))
    }

    /**
     * Checks to see if `conn` is disconnected in the given cardinal direction (NORTH, SOUTH, WEST, EAST).
     */
    fun isDisconnected(conn: UByte, dir: Direction): Boolean {
        if (dir.horizontal < 0)
            throw IllegalArgumentException("$dir is not a valid cardinal direction")
        val mask = maskForCorner(dir)
        return conn and mask == 0u.toUByte()
    }

    /**
     * Checks to see if `conn` has an internal connection in the given cardinal direction (NORTH, SOUTH, WEST, EAST).
     */
    fun isInternal(conn: UByte, dir: Direction): Boolean {
        if (dir.horizontal < 0)
            throw IllegalArgumentException("$dir is not a valid cardinal direction")
        return conn and maskForCorner(dir) == maskForInternal(dir)
    }

    /**
     * Checks to see if `conn` has an external connection in the given cardinal direction (NORTH, SOUTH, WEST, EAST).
     */
    fun isExternal(conn: UByte, dir: Direction): Boolean {
        if (dir.horizontal < 0)
            throw IllegalArgumentException("$dir is not a valid cardinal direction")
        return conn and maskForCorner(dir) == maskForExternal(dir)
    }

    /**
     * Checks to see if `conn` has a corner connection in the given cardinal direction (NORTH, SOUTH, WEST, EAST).
     */
    fun isCorner(conn: UByte, dir: Direction): Boolean {
        if (dir.horizontal < 0)
            throw IllegalArgumentException("$dir is not a valid cardinal direction")
        val mask = maskForCorner(dir)
        return conn and mask == mask
    }

    /**
     * Sets `conn` to be disconnected in the given cardinal direction (NORTH, SOUTH, WEST, EAST).
     */
    fun setDisconnected(conn: UByte, dir: Direction): UByte {
        if (dir.horizontal < 0)
            throw IllegalArgumentException("$dir is not a valid cardinal direction")
        // corner mask is all 1's, so we can use its inverse
        return conn and maskForCorner(dir).inv()
    }

    /**
     * Sets `conn` to have an internal connection in the given cardinal direction (NORTH, SOUTH, WEST, EAST).
     */
    fun setInternal(conn: UByte, dir: Direction): UByte {
        // we call setDisconnected() so cardinal checking is done there
        return setDisconnected(conn, dir) or maskForInternal(dir)
    }

    /**
     * Sets `conn` to have an external connection in the given cardinal direction (NORTH, SOUTH, WEST, EAST).
     */
    fun setExternal(conn: UByte, dir: Direction): UByte {
        // we call setDisconnected() so cardinal checking is done there
        return setDisconnected(conn, dir) or maskForExternal(dir)
    }

    /**
     * Sets `conn` to have a corner connection in the given cardinal direction (NORTH, SOUTH, WEST, EAST).
     */
    fun setCorner(conn: UByte, dir: Direction): UByte {
        if (dir.horizontal < 0)
            throw IllegalArgumentException("$dir is not a valid cardinal direction")
        // corner mask is all 1's, so we don't need to worry about setting 0's
        return conn or maskForCorner(dir)
    }

    /**
     * Rotates a set of connections so that they appear from the rotated perspective of the cardinal direction.
     *
     * If cardinal is a rotation of NORTH -> EAST, then a connection that is actually on the EAST side will appear to be
     * on the NORTH side.
     */
    fun unrotatedConnections(conn: UByte, cardinal: Direction): UByte {
        return conn.rotateRight(RotationUtils.cardinalRotatedIndex(cardinal) shl 1)
    }
}
