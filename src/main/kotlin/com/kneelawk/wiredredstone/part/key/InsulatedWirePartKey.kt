package com.kneelawk.wiredredstone.part.key

import alexiil.mc.lib.multipart.api.render.PartModelKey
import net.minecraft.util.DyeColor
import net.minecraft.util.math.Direction

data class InsulatedWirePartKey(val side: Direction, val connections: UByte, val color: DyeColor, val powered: Boolean) : PartModelKey()
