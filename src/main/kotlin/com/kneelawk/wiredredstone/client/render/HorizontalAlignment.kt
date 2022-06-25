package com.kneelawk.wiredredstone.client.render

enum class HorizontalAlignment {
    START, CENTER, END;

    fun offset(textWidth: Int): Double {
        return when (this) {
            START -> 0.0
            CENTER -> -textWidth.toDouble() / 2.0
            END -> -textWidth.toDouble()
        }
    }
}
