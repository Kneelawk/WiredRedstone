package com.kneelawk.wiredredstone.part

interface CenterBlockablePart : CenterConnectablePart {
    fun updateBlockage(blockage: UByte)
}
