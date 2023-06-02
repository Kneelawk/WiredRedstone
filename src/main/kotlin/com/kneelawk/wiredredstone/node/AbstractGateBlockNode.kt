package com.kneelawk.wiredredstone.node

import com.kneelawk.graphlib.graph.BlockNodeHolder
import com.kneelawk.graphlib.graph.NodeView
import com.kneelawk.graphlib.graph.struct.Node
import com.kneelawk.graphlib.util.SidedPos
import com.kneelawk.graphlib.wire.SidedWireBlockNode
import com.kneelawk.graphlib.wire.SidedWireConnectionFilter
import com.kneelawk.graphlib.wire.WireConnectionDiscoverers
import com.kneelawk.graphlib.wire.WireConnectionType
import com.kneelawk.wiredredstone.logic.RedstoneLogic
import com.kneelawk.wiredredstone.part.AbstractGatePart
import com.kneelawk.wiredredstone.part.SidedPart
import com.kneelawk.wiredredstone.util.NetNode
import com.kneelawk.wiredredstone.util.RotationUtils
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockView
import kotlin.reflect.KClass
import kotlin.reflect.safeCast

abstract class AbstractGateBlockNode<P : AbstractGatePart>(private val partClass: KClass<P>) : SidedWireBlockNode,
    RedstoneCarrierBlockNode {
    protected abstract val filter: SidedWireConnectionFilter

    protected abstract fun getConnectDirection(part: P): Direction

    protected fun getPart(world: BlockView, pos: BlockPos): P? {
        return SidedPart.getPart(world, SidedPos(pos, side), partClass.java)
    }

    override fun findConnections(
        world: ServerWorld, nv: NodeView, pos: BlockPos, self: Node<BlockNodeHolder>
    ): MutableCollection<Node<BlockNodeHolder>> {
        return WireConnectionDiscoverers.wireFindConnections(this, world, nv, pos, self, filter)
    }

    override fun canConnect(
        world: ServerWorld, nodeView: NodeView, pos: BlockPos, self: NetNode, other: NetNode
    ): Boolean {
        return WireConnectionDiscoverers.wireCanConnect(this, world, pos, self, other, filter)
    }

    override fun canConnect(
        world: ServerWorld, pos: BlockPos, inDirection: Direction, connectionType: WireConnectionType, self: NetNode,
        other: NetNode
    ): Boolean {
        val part = getPart(world, pos) ?: return false

        val cardinal = getConnectDirection(part)

        return RotationUtils.rotatedDirection(side, cardinal) == inDirection
    }

    override fun onConnectionsChanged(world: ServerWorld, pos: BlockPos, self: NetNode) {
        RedstoneLogic.scheduleUpdate(world, pos)
        getPart(world, pos)?.updateConnections()
    }
}
