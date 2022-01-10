package com.kneelawk.wiredredstone.part.key

import alexiil.mc.lib.multipart.api.render.PartModelKey
import net.minecraft.util.math.Direction

data class GateDiodePartKey(val side: Direction, val direction: Direction, val powered: Boolean) : PartModelKey()
