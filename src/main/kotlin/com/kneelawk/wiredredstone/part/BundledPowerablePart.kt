package com.kneelawk.wiredredstone.part

import net.minecraft.util.DyeColor
import net.minecraft.util.math.Direction

interface BundledPowerablePart {
    fun updatePower(power: ULong)

    fun updatePower(inner: DyeColor, power: Int)

    fun getPower(inner: DyeColor): Int

    fun getPower(side: Direction): ULong
}
