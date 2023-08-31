package com.kneelawk.wiredredstone.util.bits

import net.minecraft.util.math.Direction

object CenterConnectionUtils {
    const val NONE: UByte = 0u

    private fun shiftFor(dir: Direction): Int = dir.id

    private fun maskFor(dir: Direction): UByte = 1u.toUByte().rotateLeft(shiftFor(dir))

    fun test(bits: UByte, dir: Direction): Boolean {
        val mask = maskFor(dir)
        return bits and mask == mask
    }

    fun set(bits: UByte, dir: Direction): UByte {
        return bits or maskFor(dir)
    }

    fun reset(bits: UByte, dir: Direction): UByte {
        return bits and maskFor(dir).inv()
    }
}
