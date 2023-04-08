package com.kneelawk.wiredredstone.node

import com.kneelawk.graphlib.api.v1.node.BlockNode
import com.kneelawk.wiredredstone.WRLog
import com.kneelawk.wiredredstone.util.maybeGetByte
import com.kneelawk.wiredredstone.util.toByte
import com.kneelawk.wiredredstone.util.toEnum
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.util.math.Direction

object BlockNodeUtil {
    inline fun <reified T : Enum<T>> readSidedTyped(
        tag: NbtElement?, constructor: (side: Direction, type: T, tag: NbtCompound) -> BlockNode
    ): BlockNode? {
        if (tag !is NbtCompound) {
            WRLog.warn("tag is not a compound tag")
            return null
        }

        val side = Direction.byId((tag.maybeGetByte("side") ?: run {
            WRLog.warn("missing 'side' tag")
            return null
        }).toInt())

        val type = (tag.maybeGetByte("type") ?: run {
            WRLog.warn("missing 'type' tag")
            return null
        }).toEnum<T>()

        return constructor(side, type, tag)
    }

    inline fun <reified T : Enum<T>> writeSidedType(
        side: Direction, type: T, writer: (NbtCompound) -> Unit = {}
    ): NbtCompound {
        val tag = NbtCompound()
        tag.putByte("side", side.id.toByte())
        tag.putByte("type", type.toByte())
        writer(tag)
        return tag
    }
}
