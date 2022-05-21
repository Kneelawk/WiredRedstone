package com.kneelawk.wiredredstone.part

import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.text.Text
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos

interface WRPart {
    fun getPos(): BlockPos

    @Environment(EnvType.CLIENT)
    fun getPartName(hitResult: BlockHitResult?): Text
}
