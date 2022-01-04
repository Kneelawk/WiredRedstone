package com.kneelawk.wiredredstone.util

import com.kneelawk.wiredredstone.part.AbstractWirePart
import com.kneelawk.wiredredstone.wirenet.SidedPartExt
import com.kneelawk.wiredredstone.wirenet.WirePartExt
import com.kneelawk.wiredredstone.wirenet.getWireNetworkState
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.Direction

object WireUtils {
    enum class ConnectionType {
        INTERNAL,
        EXTERNAL,
        CORNER
    }

    data class Connection(val edge: Direction, val type: ConnectionType) {
        fun setForSide(side: Direction, connections: UByte): UByte? {
            val cardinal = RotationUtils.unrotatedDirection(side, edge)
            if (!ConnectionUtils.isValid(cardinal)) {
                return null
            }

            return when (type) {
                ConnectionType.INTERNAL -> ConnectionUtils.setInternal(connections, cardinal)
                ConnectionType.EXTERNAL -> ConnectionUtils.setExternal(connections, cardinal)
                ConnectionType.CORNER -> ConnectionUtils.setCorner(connections, cardinal)
            }
        }
    }

    /**
     * Updates visual connections, updating the LMP part and asking it to update the client.
     */
    fun updateClientWire(world: ServerWorld, pos: SidedPos) {
        val side = pos.side
        val part = AbstractWirePart.getWire(world, pos) ?: return
        val net = world.getWireNetworkState().controller

        val nodes1 = net.getNodesAt(pos)
            .filter { it.data.ext is WirePartExt }
            // The flatMap here causes entire parts to visually connect, even if only one of their network-nodes is
            // actually connected.
            .flatMap { node ->
                node.connections.mapNotNull { link ->
                    val other = link.other(node)
                    if (node.data.pos == other.data.pos && other.data.ext is SidedPartExt) {
                        Connection(other.data.ext.side, ConnectionType.INTERNAL)
                    } else {
                        other.data.pos.subtract(node.data.pos.offset(side)).let { Direction.fromVector(it) }
                            ?.let { Connection(it, ConnectionType.CORNER) }
                            ?: other.data.pos.subtract(node.data.pos).let { Direction.fromVector(it) }
                                ?.let { Connection(it, ConnectionType.EXTERNAL) }
                    }
                }
            }
            .groupBy { it.edge }
            .mapNotNull { (_, v) -> v.distinct().singleOrNull() }

        val connections = nodes1.fold(0u.toUByte()) { current, new -> new.setForSide(side, current) ?: current }
        val newConnections = part.overrideConnections(connections)

        part.updateConnections(newConnections)
        part.redraw()
    }
}