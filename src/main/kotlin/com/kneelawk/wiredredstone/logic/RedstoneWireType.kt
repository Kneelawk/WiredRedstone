package com.kneelawk.wiredredstone.logic

import net.minecraft.util.DyeColor

sealed class RedstoneWireType {
    object RedAlloy : RedstoneWireType()
    data class Colored(val color: DyeColor) : RedstoneWireType()
    data class Bundled(val color: DyeColor?, val inner: DyeColor) : RedstoneWireType()

    fun canConnect(other: RedstoneWireType): Boolean {
        if (this == other) return true
        if (this == RedAlloy && other is Colored || this is Colored && other == RedAlloy) return true
        if (this is Colored && other is Bundled && other.inner == this.color || this is Bundled && other is Colored && this.inner == other.color) return true
        if (other is Bundled && this == Bundled(null, other.inner) || this is Bundled && other == Bundled(
                null, this.inner
            )
        ) return true
        return false
    }
}
