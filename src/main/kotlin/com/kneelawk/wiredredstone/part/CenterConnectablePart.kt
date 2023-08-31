package com.kneelawk.wiredredstone.part

interface CenterConnectablePart : CenterPart, BlockingPart {
    fun updateConnections(connections: UByte)

    fun overrideConnections(connections: UByte): UByte
}
