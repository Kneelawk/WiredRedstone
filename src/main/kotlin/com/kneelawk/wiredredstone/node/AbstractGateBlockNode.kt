package com.kneelawk.wiredredstone.node

import com.kneelawk.graphlib.api.v1.graph.GraphView
import com.kneelawk.graphlib.api.v1.graph.NodeHolder
import com.kneelawk.graphlib.api.v1.util.SidedPos
import com.kneelawk.graphlib.api.v1.util.graph.Node
import com.kneelawk.graphlib.api.v1.wire.SidedWireBlockNode
import com.kneelawk.graphlib.api.v1.wire.SidedWireConnectionFilter
import com.kneelawk.graphlib.api.v1.wire.WireConnectionDiscoverers
import com.kneelawk.graphlib.api.v1.wire.WireConnectionType
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
        return partClass.safeCast(SidedPart.getPart(world, SidedPos(pos, side)))
    }

    override fun findConnections(
        world: ServerWorld, nv: GraphView, pos: BlockPos, self: Node<NodeHolder>
    ): MutableCollection<Node<NodeHolder>> {
        return WireConnectionDiscoverers.wireFindConnections(this, world, nv, pos, self, filter)
    }

    override fun canConnect(
        world: ServerWorld, nodeView: GraphView, pos: BlockPos, self: NetNode, other: NetNode
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

    override fun onConnectionsChanged(world: ServerWorld, gv: GraphView, pos: BlockPos, self: NetNode) {
        RedstoneLogic.scheduleUpdate(world, pos)
        getPart(world, pos)?.updateConnections()
    }
}
