package com.kneelawk.wiredredstone.part.key

import alexiil.mc.lib.multipart.api.render.PartModelKey
import net.minecraft.util.math.Direction

data class GateRSLatchPartKey(
    val side: Direction, val direction: Direction, val connections: UByte, val latchSetState: Boolean,
    val outputEnabled: Boolean, val inputSetPowered: Boolean, val inputResetPowered: Boolean,
    val outputSetPowered: Boolean, val outputResetPowered: Boolean
) : PartModelKey()
