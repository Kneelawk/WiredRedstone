package com.kneelawk.wiredredstone.part.key

import alexiil.mc.lib.multipart.api.render.PartModelKey
import net.minecraft.util.math.Direction

data class GateProjectorSimplePartKey(
    val side: Direction, val direction: Direction, val connections: UByte, val powered: Boolean, val distance: Int
) : PartModelKey()
