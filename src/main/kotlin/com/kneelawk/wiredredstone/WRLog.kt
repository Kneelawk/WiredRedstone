package com.kneelawk.wiredredstone

import org.apache.logging.log4j.LogManager

object WRLog {
    val log = LogManager.getLogger(WRConstants.MOD_ID)

    fun warn(msg: Any?) {
        log.warn(msg)
    }
}