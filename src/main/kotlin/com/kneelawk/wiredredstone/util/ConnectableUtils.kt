package com.kneelawk.wiredredstone.util

import alexiil.mc.lib.multipart.api.MultipartUtil
import com.kneelawk.wiredredstone.part.BlockablePart
import com.kneelawk.wiredredstone.part.ConnectablePart
import com.kneelawk.wiredredstone.part.RedrawablePart
import com.kneelawk.wiredredstone.wirenet.ConnectablePartExt
import com.kneelawk.wiredredstone.wirenet.SidedPartExt
import com.kneelawk.wiredredstone.wirenet.WireNetworkController
import com.kneelawk.wiredredstone.wirenet.getWireNetworkState
import net.minecraft.block.BlockState
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.world.BlockView

object ConnectableUtils {
    data class Connection(val edge: Direction, val type: ConnectionType) {
        fun setForSide(side: Direction, connections: UByte, blockage: UByte = BlockageUtils.UNBLOCKED): UByte {
            val cardinal = RotationUtils.unrotatedDirection(side, edge)
            if (!DirectionUtils.isHorizontal(cardinal) || BlockageUtils.isBlocked(blockage, cardinal)) {
                // A blockage or a vertical connection means we shouldn't set anything
                return connections
            }

            return when (type) {
                ConnectionType.INTERNAL -> ConnectionUtils.setInternal(connections, cardinal)
                ConnectionType.EXTERNAL -> ConnectionUtils.setExternal(connections, cardinal)
                ConnectionType.CORNER -> ConnectionUtils.setCorner(connections, cardinal)
            }
        }
    }

    /**
     * Updates both the connections and the blockage of the LMP part and asks it to update the client.
     *
     * This expects the part at the given side-pos to be a `BlockablePart`.
     */
    fun updateBlockageAndConnections(world: ServerWorld, part: BlockablePart, wireWidth: Double, wireHeight: Double) {
        val blockPos = part.getPos()
        val side = part.side
        val net = world.getWireNetworkState().controller

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

        updateConnectionsImpl(part, net, blockage)
    }

    /**
     * Updates connections of the LMP part and asking it to update the client.
     */
    fun updateConnections(world: ServerWorld, part: ConnectablePart) {
        val net = world.getWireNetworkState().controller

        updateConnectionsImpl(part, net, BlockageUtils.UNBLOCKED)
    }

    /**
     * Updates visual connections, updating the LMP part and asking it to update the client.
     */
    private fun updateConnectionsImpl(
        part: ConnectablePart, net: WireNetworkController, blockage: UByte
    ) {
        val side = part.side
        val pos = part.getSidedPos()

        val nodes1 = net.getNodesAt(pos).filter { it.data.ext is ConnectablePartExt }
            // The flatMap here causes entire parts to visually connect, even if only one of their network-nodes is
            // actually connected.
            .flatMap { node ->
                node.connections.mapNotNull { link ->
                    val other = link.other(node)
                    if (node.data.pos == other.data.pos && other.data.ext is SidedPartExt) {
                        Connection(other.data.ext.side, ConnectionType.INTERNAL)
                    } else {
                        other.data.pos.subtract(node.data.pos.offset(side)).let { Direction.fromVector(it) }
                            ?.let { Connection(it, ConnectionType.CORNER) } ?: other.data.pos.subtract(node.data.pos)
                            .let { Direction.fromVector(it) }?.let { Connection(it, ConnectionType.EXTERNAL) }
                    }
                }
            }.groupBy { it.edge }.mapNotNull { (_, v) -> v.distinct().singleOrNull() }

        val connections = nodes1.fold(0u.toUByte()) { current, new -> new.setForSide(side, current, blockage) }
        val newConnections = part.overrideConnections(connections)

        part.updateConnections(newConnections)
        (part as? RedrawablePart)?.redraw()
    }

    /**
     * Checks whether a side of a block is valid to have a wire on.
     */
    fun isValidFace(state: BlockState, world: BlockView, pos: BlockPos, side: Direction): Boolean {
        return state.isSideSolidFullSquare(world, pos, side)
    }

    /**
     * Checks whether a wire on the given side, of the given dimensions, can connect in a given direction.
     */
    fun canWireConnect(
        world: BlockView, pos: BlockPos, inDirection: Direction, type: ConnectionType, wireSide: Direction,
        wireWidth: Double, wireHeight: Double
    ): Boolean {
        val cardinal = RotationUtils.unrotatedDirection(wireSide, inDirection)
        val inside =
            BoundingBoxUtils.getWireInsideConnectionShape(wireSide, cardinal, wireWidth, wireHeight) ?: return true
        if (checkInside(world, pos, inside)) {
            return false
        }

        return if (type == ConnectionType.CORNER) {
            val outside =
                BoundingBoxUtils.getWireOutsideConnectionShape(wireSide, cardinal, wireWidth, wireHeight, true)
                    ?: return true
            val checkOutside = checkOutside(world, pos.offset(inDirection), outside)
            return !checkOutside
        } else {
            true
        }
    }

    /**
     * Checks if any parts are conflicting with the given shape box inside the pos given.
     *
     * @param world The world to check for conflicting parts within.
     * @param pos The position of the blockspace to check for conflicting parts within.
     * @param shape The part of the connection that is within the checking block's block-space.
     * @return True if the shape **does** conflict with anything.
     */
    fun checkInside(world: BlockView, pos: BlockPos, shape: Box): Boolean {
        return MultipartUtil.get(world, pos)?.allParts?.any {
            val blocking = if (it is ConnectablePart) {
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
                val blocking = if (it is ConnectablePart) {
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