package com.kneelawk.wiredredstone

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

object WRLog {
    val log: Logger = LogManager.getLogger(WRConstants.MOD_ID)

    fun warn(msg: Any?) {
        log.warn(msg)
    }
}