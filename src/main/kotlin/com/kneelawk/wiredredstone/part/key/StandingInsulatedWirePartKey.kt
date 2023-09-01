package com.kneelawk.wiredredstone.part.key

import alexiil.mc.lib.multipart.api.render.PartModelKey
import net.minecraft.util.DyeColor

data class StandingInsulatedWirePartKey(val color: DyeColor, val connections: UByte, val powered: Boolean) :
    PartModelKey()
