package com.kneelawk.wiredredstone.part.key

import alexiil.mc.lib.multipart.api.render.PartModelKey
import net.minecraft.util.math.Direction

data class GateXorPartKey(
    val side: Direction, val direction: Direction, val connections: UByte, val inputRightPowered: Boolean,
    val inputLeftPowered: Boolean, val outputPowered: Boolean
) : PartModelKey()
