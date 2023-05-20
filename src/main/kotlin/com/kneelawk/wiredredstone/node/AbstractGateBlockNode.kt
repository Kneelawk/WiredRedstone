package com.kneelawk.wiredredstone.node

import com.kneelawk.graphlib.api.graph.GraphView
import com.kneelawk.graphlib.api.graph.NodeHolder
import com.kneelawk.graphlib.api.node.BlockNode
import com.kneelawk.graphlib.api.node.KeyBlockNode
import com.kneelawk.graphlib.api.util.SidedPos
import com.kneelawk.graphlib.api.wire.SidedWireBlockNode
import com.kneelawk.graphlib.api.wire.SidedWireConnectionFilter
import com.kneelawk.graphlib.api.wire.WireConnectionDiscoverers
import com.kneelawk.graphlib.api.wire.WireConnectionType
import com.kneelawk.wiredredstone.logic.RedstoneLogic
import com.kneelawk.wiredredstone.part.AbstractGatePart
import com.kneelawk.wiredredstone.part.SidedPart
import com.kneelawk.wiredredstone.util.RotationUtils
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockView
import kotlin.reflect.KClass
import kotlin.reflect.safeCast

abstract class AbstractGateBlockNode<P : AbstractGatePart>(private val partClass: KClass<P>) : SidedWireBlockNode,
    RedstoneCarrierBlockNode, KeyBlockNode {
    protected abstract val filter: SidedWireConnectionFilter

    protected abstract fun getConnectDirection(part: P): Direction

    protected fun getPart(world: BlockView, pos: BlockPos): P? {
        return partClass.safeCast(SidedPart.getPart(world, SidedPos(pos, side)))
    }

    override fun findConnections(
        self: NodeHolder<BlockNode>, world: ServerWorld, graphView: GraphView
    ): MutableCollection<NodeHolder<BlockNode>> {
        return WireConnectionDiscoverers.wireFindConnections(this, self, world, graphView, filter)
    }

    override fun canConnect(
        self: NodeHolder<BlockNode>, world: ServerWorld, nodeView: GraphView, other: NodeHolder<BlockNode>
    ): Boolean {
        return WireConnectionDiscoverers.wireCanConnect(this, self, world, other, filter)
    }

    override fun canConnect(
        self: NodeHolder<BlockNode>, world: ServerWorld, inDirection: Direction, connectionType: WireConnectionType,
        other: NodeHolder<BlockNode>
    ): Boolean {
        val part = getPart(world, self.pos) ?: return false

        val cardinal = getConnectDirection(part)

        return RotationUtils.rotatedDirection(side, cardinal) == inDirection
    }

    override fun onConnectionsChanged(self: NodeHolder<BlockNode>, world: ServerWorld, gv: GraphView) {
        RedstoneLogic.scheduleUpdate(world, self.pos)
        getPart(world, self.pos)?.updateConnections()
    }
}
