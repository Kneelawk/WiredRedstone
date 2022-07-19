package com.kneelawk.wiredredstone.part.key

import alexiil.mc.lib.multipart.api.render.PartModelKey
import net.minecraft.util.math.Direction

data class GateAndPartKey(
    val side: Direction, val direction: Direction, val connections: UByte, val inputRightPowered: Boolean,
    val inputBackPowered: Boolean, val inputLeftPowered: Boolean, val outputPowered: Boolean,
    val inputRightEnabled: Boolean, val inputBackEnabled: Boolean, val inputLeftEnabled: Boolean
) : PartModelKey()
