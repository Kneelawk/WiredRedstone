package com.kneelawk.wiredredstone.part.key

import alexiil.mc.lib.multipart.api.render.PartModelKey
import net.minecraft.util.math.Direction

data class GateNotPartKey(
    val side: Direction, val direction: Direction, val connections: UByte, val inputPowered: Boolean,
    val outputPowered: Boolean
) : PartModelKey()
