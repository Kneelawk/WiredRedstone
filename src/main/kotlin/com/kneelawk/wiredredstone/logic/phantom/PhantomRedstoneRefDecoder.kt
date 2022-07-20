package com.kneelawk.wiredredstone.logic.phantom

import net.minecraft.nbt.NbtElement

interface PhantomRedstoneRefDecoder {
    fun decode(nbt: NbtElement?): PhantomRedstoneRef?
}
