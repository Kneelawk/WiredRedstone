package com.kneelawk.wiredredstone.util

import net.fabricmc.fabric.api.util.NbtType
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement

fun NbtCompound.maybeGetByte(key: String): Byte? {
    return if (contains(key, NbtType.BYTE)) {
        getByte(key)
    } else {
        null
    }
}

fun NbtCompound.getBoolean(key: String, default: Boolean): Boolean =
    if (contains(key, NbtElement.NUMBER_TYPE.toInt())) getBoolean(key) else default
