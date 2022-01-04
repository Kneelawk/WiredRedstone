package com.kneelawk.wiredredstone

import net.minecraft.util.Identifier

object WRConstants {
    val MOD_ID = "wiredredstone"

    fun id(path: String): Identifier {
        return Identifier(MOD_ID, path)
    }

    fun str(path: String): String {
        return "$MOD_ID:$path"
    }
}
