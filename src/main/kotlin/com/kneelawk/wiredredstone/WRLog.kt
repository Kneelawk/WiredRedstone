package com.kneelawk.wiredredstone

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

object WRLog {
    val log: Logger = LogManager.getLogger(WRConstants.MOD_ID)

    fun warn(msg: Any?) {
        log.warn(msg)
    }

    fun warn(msg: String, p0: Any?, p1: Any?) {
        log.warn(msg, p0, p1)
    }

    @JvmStatic
    fun error(msg: String, p0: Any?, p1: Any?, p2: Any?) {
        log.error(msg, p0, p1, p2)
    }
}
