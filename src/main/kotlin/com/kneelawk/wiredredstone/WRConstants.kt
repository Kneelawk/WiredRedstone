package com.kneelawk.wiredredstone

import net.minecraft.text.MutableText
import net.minecraft.text.TranslatableText
import net.minecraft.util.Identifier

object WRConstants {
    const val MOD_ID = "wiredredstone"
    const val SHADER_PREFIX = "shaders/core/"
    const val SHADER_CHECK_PREFIX = "shaders/core/$MOD_ID:"

    fun id(path: String): Identifier {
        return Identifier(MOD_ID, path)
    }

    fun str(path: String): String {
        return "$MOD_ID:$path"
    }

    fun tt(prefix: String, path: String, vararg args: Any?): MutableText {
        return TranslatableText("$prefix.$MOD_ID.$path", *args)
    }

    fun tooltip(path: String, vararg args: Any?): MutableText {
        return tt("tooltip", path, *args)
    }

    fun gui(path: String, vararg args: Any?): MutableText {
        return tt("gui", path, *args)
    }
}
