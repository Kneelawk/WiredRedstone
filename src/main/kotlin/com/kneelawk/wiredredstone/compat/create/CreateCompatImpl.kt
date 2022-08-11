package com.kneelawk.wiredredstone.compat.create

import com.kneelawk.wiredredstone.WRLog

@Suppress("unused")
object CreateCompatImpl : CreateCompat {
    override fun init() {
        WRLog.log.info("Wired Redstone: enabling Create compatibility!")
    }
}
