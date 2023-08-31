package com.kneelawk.wiredredstone.part

// The way connections are specified, it doesn't really make sense for the part not to be sided.
interface ConnectablePart : SidedPart, BlockingPart {
    fun updateConnections(connections: UByte)

    fun overrideConnections(connections: UByte): UByte
}
