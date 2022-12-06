package com.kneelawk.wiredredstone.part

interface NoBreakPart {
    /**
     * Sets that this part should ignore all structural checks until the next time it receives a world tick.
     *
     * Note: The set value should also carry across NBT saves/loads.
     */
    fun setNoBreak()
}
