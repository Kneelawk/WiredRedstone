package com.kneelawk.wiredredstone.part

interface BlockablePart : ConnectablePart {
    fun updateBlockage(blockage: UByte)
}