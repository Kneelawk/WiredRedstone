package com.kneelawk.wiredredstone.util.connectable

import alexiil.mc.lib.multipart.api.AbstractPart
import alexiil.mc.lib.multipart.api.MultipartUtil
import com.kneelawk.graphlib.api.graph.GraphView
import com.kneelawk.graphlib.api.graph.user.BlockNode
import com.kneelawk.graphlib.api.graph.user.SidedBlockNode
import com.kneelawk.graphlib.api.util.HalfLink
import com.kneelawk.graphlib.api.wire.CenterWireBlockNode
import com.kneelawk.graphlib.api.wire.SidedWireBlockNode
import com.kneelawk.graphlib.api.wire.WireConnectionType
import com.kneelawk.wiredredstone.config.CommonConfig
import com.kneelawk.wiredredstone.node.PartBlockNode
import com.kneelawk.wiredredstone.node.WRBlockNodes.WIRE_NET
import com.kneelawk.wiredredstone.part.*
import com.kneelawk.wiredredstone.util.BoundingBoxUtils
import com.kneelawk.wiredredstone.util.DirectionUtils
import com.kneelawk.wiredredstone.util.RotationUtils
import com.kneelawk.wiredredstone.util.bits.BlockageUtils
import com.kneelawk.wiredredstone.util.bits.CenterConnectionUtils
import com.kneelawk.wiredredstone.util.bits.ConnectionUtils
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.world.BlockView
import kotlin.streams.asSequence

// Significant parts of this were copied or inspired by 2xsaiko's HCTM-Base.

object ConnectableUtils {

    /**
     * Updates both the connections and the blockage of the LMP part and asks it to update the client.
     *
     * This expects the part at the given side-pos to be a `BlockablePart`.
     */
    fun updateBlockageAndConnections(world: ServerWorld, part: BlockablePart, wireWidth: Double, wireHeight: Double) {
        val blockPos = part.getPos()
        val side = part.side
        val net = WIRE_NET.getGraphView(world)

        val blockage = DirectionUtils.HORIZONTALS.fold(0u.toUByte()) { blockage, cardinal ->
            val inside = BoundingBoxUtils.getWireInsideConnectionShape(side, cardinal, wireWidth, wireHeight)
                ?: return@fold blockage
            if (checkInside(world, blockPos, inside)) {
                BlockageUtils.setBlocked(blockage, cardinal)
            } else {
                blockage
            }
        }

        part.updateBlockage(blockage)

        updateConnectionsImpl(part, net)
    }

    fun updateBlockageAndConnections(
        world: ServerWorld, part: CenterBlockablePart, wireDiameter: Double
    ) {
        val blockPos = part.getPos()
        val net = WIRE_NET.getGraphView(world)

        var blockage = 0u.toUByte()
        for (dir in Direction.values()) {
            val inside = BoundingBoxUtils.getCenterWireInsideConnectionShape(dir, wireDiameter)
            if (checkInside(world, blockPos, inside)) {
                blockage = CenterConnectionUtils.set(blockage, dir)
            }
        }

        part.updateBlockage(blockage)

        updateConnectionsImpl(part, net)
    }

    /**
     * Updates connections of the LMP part and asking it to update the client.
     */
    fun updateConnections(world: ServerWorld, part: ConnectablePart) {
        val net = WIRE_NET.getGraphView(world)

        updateConnectionsImpl(part, net)
    }

    /**
     * Updates visual connections, updating the LMP part and asking it to update the client.
     */
    private fun updateConnectionsImpl(part: ConnectablePart, net: GraphView) {
        val side = part.side
        val pos = part.getSidedPos()

        // FIXME: this just assumes all nodes at the pos are the nodes we care about. There should be a filter.
        val connections = net.getNodesAt(pos).asSequence().filter { it.node is SidedWireBlockNode }
            // The fold here causes entire parts to visually connect, even if only one of their network-nodes is
            // actually connected. The first byte represents the actual connection value. The second byte keeps track of
            // if this edge has already tried to connect in a different connection.
            .fold(Pair(0u.toUByte(), 0u.toUByte())) { connections, node ->
                var newConn = connections
                node.connections.forEach { link ->
                    val other = link.other(node.cast(BlockNode::class.java))
                    if (node.blockPos == other.blockPos && other.node is SidedBlockNode) {
                        newConn = setSingularConnection(
                            newConn, side, (other.node as SidedBlockNode).side, ConnectionUtils::setInternal,
                            ConnectionUtils::isInternal
                        )
                    } else {
                        val cornerEdge =
                            other.blockPos.subtract(node.blockPos.offset(side)).let(DirectionUtils::fromVector)
                        if (cornerEdge != null) {
                            newConn = setSingularConnection(
                                newConn, side, cornerEdge, ConnectionUtils::setCorner, ConnectionUtils::isCorner
                            )
                        } else {
                            other.blockPos.subtract(node.blockPos).let(DirectionUtils::fromVector)?.let {
                                newConn = setSingularConnection(
                                    newConn, side, it, ConnectionUtils::setExternal, ConnectionUtils::isExternal
                                )
                            }
                        }
                    }
                }
                newConn
            }

        val newConnections = part.overrideConnections(connections.first)

        part.updateConnections(newConnections)
        (part as? RedrawablePart)?.let {
            it.redraw()
            it.reshape()
        }
    }

    private fun updateConnectionsImpl(part: CenterConnectablePart, net: GraphView) {
        val pos = part.getPos()

        // FIXME: this just assumes all nodes at the pos are the nodes we care about. There should be a filter.
        var connections = 0u.toUByte()
        for (holder in net.getNodesAt(pos)) {
            if (holder.node !is CenterWireBlockNode) continue

            for (link in holder.connections) {
                val other = link.other(holder)
                val otherPos = other.blockPos
                if (otherPos == pos) {
                    val node = other.node as? SidedBlockNode ?: continue
                    val dir = node.side

                    connections = CenterConnectionUtils.set(connections, dir)
                } else {
                    val dir = DirectionUtils.fromVector(otherPos.subtract(pos)) ?: continue

                    connections = CenterConnectionUtils.set(connections, dir)
                }
            }
        }

        val newConnections = part.overrideConnections(connections)

        part.updateConnections(newConnections)
        if (part is RedrawablePart) {
            part.redraw()
            part.reshape()
        }
    }

    private inline fun setSingularConnection(
        connections: Pair<UByte, UByte>, side: Direction, edge: Direction, set: (UByte, Direction) -> UByte,
        test: (UByte, Direction) -> Boolean
    ): Pair<UByte, UByte> {
        val cardinal = RotationUtils.unrotatedDirection(side, edge)

        if (!DirectionUtils.isHorizontal(cardinal)) {
            return connections
        }

        return if (!ConnectionUtils.isDisconnected(connections.second, cardinal) && !test(
                connections.second, cardinal
            )
        ) {
            Pair(ConnectionUtils.setDisconnected(connections.first, cardinal), connections.second)
        } else {
            Pair(set(connections.first, cardinal), set(connections.second, cardinal))
        }
    }

    fun shouldUpdateConnectionsForNeighborUpdate(
        shapeCache: MutableMap<BlockPos, VoxelShape>, world: ServerWorld, yourPos: BlockPos, otherPos: BlockPos
    ): Boolean {
        return shouldUpdateForNeighborUpdate(shapeCache, yourPos, otherPos, {
            val state = world.getBlockState(otherPos)
            state.getOutlineShape(world, otherPos)
        }, { prev, cur ->
            // Identity checking here is about the best we can do if we want to be fast. This works because most
            // blocks re-use the same VoxelShape objects if they want to keep their shapes.
            prev !== cur
        })
    }

    inline fun <T> shouldUpdateForNeighborUpdate(
        cache: MutableMap<BlockPos, T>, yourPos: BlockPos, otherPos: BlockPos, getCur: () -> T,
        compare: (T, T) -> Boolean
    ): Boolean {
        val offset = otherPos.subtract(yourPos)
        val previous = cache[offset]
        val current = getCur()

        return if (previous != null) {
            if (compare(previous, current)) {
                cache[offset] = current
                true
            } else false
        } else {
            cache[offset] = current
            true
        }
    }

    /**
     * Checks whether a wire on the given side, of the given dimensions, can make a corner connection in the given
     * direction.
     */
    fun canWireCornerConnect(
        world: BlockView, pos: BlockPos, inDirection: Direction, type: WireConnectionType, wireSide: Direction,
        wireWidth: Double, wireHeight: Double
    ): Boolean {
        val cardinal = RotationUtils.unrotatedDirection(wireSide, inDirection)

        return if (type == WireConnectionType.CORNER && !CommonConfig.current.ignoreCornerBlockage) {
            val outside =
                BoundingBoxUtils.getWireOutsideConnectionShape(wireSide, cardinal, wireWidth, wireHeight, true)
                    ?: return true
            !checkOutside(world, pos.offset(inDirection), outside)
        } else true
    }

    /**
     * Checks whether a wire on the given side, of the given dimensions, can connect in a given direction.
     */
    fun canWireConnect(
        world: BlockView, pos: BlockPos, inDirection: Direction, type: WireConnectionType, link: HalfLink,
        wireSide: Direction,
        wireWidth: Double, wireHeight: Double
    ): Boolean {
        val cardinal = RotationUtils.unrotatedDirection(wireSide, inDirection)
        val inside =
            BoundingBoxUtils.getWireInsideConnectionShape(wireSide, cardinal, wireWidth, wireHeight) ?: return true
        val part = (link.other.node as? PartBlockNode)?.getPart(link.other)
        if (checkInside(world, pos, inside, part)) {
            return false
        }

        return if (type == WireConnectionType.CORNER && !CommonConfig.current.ignoreCornerBlockage) {
            val outside =
                BoundingBoxUtils.getWireOutsideConnectionShape(wireSide, cardinal, wireWidth, wireHeight, true)
                    ?: return true
            return !checkOutside(world, pos.offset(inDirection), outside)
        } else true
    }

    /**
     * Checks whether a center wire of the given dimensions can connect in the given direction.
     */
    fun canCenterWireConnect(
        world: BlockView, pos: BlockPos, inDirection: Direction, link: HalfLink, wireDiameter: Double
    ): Boolean {
        val inside = BoundingBoxUtils.getCenterWireInsideConnectionShape(inDirection, wireDiameter)
        val part = (link.other.node as? PartBlockNode)?.getPart(link.other)
        return !checkInside(world, pos, inside, part)
    }

    /**
     * Checks if any parts are conflicting with the given shape box inside the pos given.
     *
     * @param world The world to check for conflicting parts within.
     * @param pos The position of the blockspace to check for conflicting parts within.
     * @param shape The part of the connection that is within the checking block's block-space.
     * @return True if the shape **does** conflict with anything.
     */
    fun checkInside(world: BlockView, pos: BlockPos, shape: Box, except: AbstractPart? = null): Boolean {
        return MultipartUtil.get(world, pos)?.allParts?.any {
            if (except != null && it.holder.uniqueId == except.holder.uniqueId) return@any false

            val blocking = if (it is BlockingPart) {
                it.getConnectionBlockingShape()
            } else {
                it.shape
            }
            isCollidingWith(blocking, shape)
        } ?: false
    }

    /**
     * Checks if anything is conflicting with the given shape box at the pos given.
     *
     * This check is for corner connections.
     *
     * @param world The world to check for conflicting parts within.
     * @param pos The position that the possibly conflicting block could be at, **not** the position of the checking block.
     * @param shape The part of the corner connection that is outside both connecting blocks' block-spaces.
     * @return True if the shape **does** conflict with anything.
     */
    fun checkOutside(world: BlockView, pos: BlockPos, shape: Box): Boolean {
        val multipart = MultipartUtil.get(world, pos)
        return if (multipart != null) {
            multipart.allParts.any {
                val blocking = if (it is BlockingPart) {
                    it.getConnectionBlockingShape()
                } else {
                    it.shape
                }
                isCollidingWith(blocking, shape)
            }
        } else {
            val state = world.getBlockState(pos)
            val outlineShape = state.getOutlineShape(world, pos)
            isCollidingWith(outlineShape, shape)
        }
    }

    private fun isCollidingWith(shape: VoxelShape, toCollideWith: Box): Boolean {
        for (box in shape.boundingBoxes) {
            if (box.intersects(toCollideWith)) {
                return true
            }
        }
        return false
    }
}
