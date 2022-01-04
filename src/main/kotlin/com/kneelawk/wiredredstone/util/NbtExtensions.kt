package com.kneelawk.wiredredstone.util

import net.fabricmc.fabric.api.util.NbtType
import net.minecraft.nbt.NbtCompound

fun NbtCompound.maybeGetByte(key: String): Byte? {
    return if (contains(key, NbtType.BYTE)) {
        getByte(key)
    } else {
        null
    }
}
