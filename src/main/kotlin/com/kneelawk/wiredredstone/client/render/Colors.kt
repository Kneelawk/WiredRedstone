package com.kneelawk.wiredredstone.client.render

object Colors {
    const val WHITE = 0xFFFFFFFFu
    const val GRAY = 0xFF7F7F7Fu

    fun portText(enabled: Boolean): UInt = if (enabled) WHITE else GRAY
}
