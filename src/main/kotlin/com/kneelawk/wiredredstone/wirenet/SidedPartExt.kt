package com.kneelawk.wiredredstone.wirenet

import net.minecraft.util.math.Direction

interface SidedPartExt : PartExt {
    val side: Direction
}