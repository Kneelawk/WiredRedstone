package com.kneelawk.wiredredstone.part.event

import alexiil.mc.lib.multipart.api.event.MultipartEvent

/**
 * Currently fired before Create attempts to move a multipart.
 */
class WRPartPreMoveEvent : MultipartEvent() {
    var movementNecessary: Boolean = false
        private set

    fun setMovementNecessary() {
        movementNecessary = true
    }
}
