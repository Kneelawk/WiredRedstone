package com.kneelawk.wiredredstone.util

import java.util.*

fun <T> T?.requireNotNull(msg: String): T {
    return Objects.requireNonNull(this, msg)!!
}
