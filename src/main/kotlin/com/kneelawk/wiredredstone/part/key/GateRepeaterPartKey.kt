package com.kneelawk.wiredredstone.part.key

import alexiil.mc.lib.multipart.api.render.PartModelKey
import net.minecraft.util.math.Direction

data class GateRepeaterPartKey(
    val side: Direction, val direction: Direction, val connections: UByte, val delay: Int, val inputPowered: Boolean,
    val outputTorch: Boolean, val outputPowered: Boolean
) : PartModelKey()
