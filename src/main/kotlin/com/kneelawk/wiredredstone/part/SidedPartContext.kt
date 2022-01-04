package com.kneelawk.wiredredstone.part

import alexiil.mc.lib.multipart.api.MultipartEventBus
import net.minecraft.util.math.Direction

class SidedPartContext(private val bus: MultipartEventBus) {
    // There will only ever be one AbstractNetworkedPart per side
    private val parts = arrayOfNulls<AbstractSidedPart?>(6)

    fun setPart(side: Direction, part: AbstractSidedPart?) {
        parts[side.id] = part

        if (part == null && parts.all { it == null }) {
            bus.removeListeners(this)
        }
    }

    fun getPart(side: Direction): AbstractSidedPart? {
        return parts[side.id]
    }
}