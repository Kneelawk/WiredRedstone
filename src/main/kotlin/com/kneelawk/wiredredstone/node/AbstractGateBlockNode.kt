package com.kneelawk.wiredredstone.node

import com.kneelawk.graphlib.api.graph.NodeContext
import com.kneelawk.graphlib.api.graph.NodeHolder
import com.kneelawk.graphlib.api.node.BlockNode
import com.kneelawk.graphlib.api.util.SidedPos
import com.kneelawk.graphlib.api.wire.SidedWireBlockNode
import com.kneelawk.graphlib.api.wire.SidedWireConnectionFilter
import com.kneelawk.graphlib.api.wire.WireConnectionDiscoverers
import com.kneelawk.graphlib.api.wire.WireConnectionType
import com.kneelawk.wiredredstone.logic.RedstoneLogic
import com.kneelawk.wiredredstone.part.AbstractGatePart
import com.kneelawk.wiredredstone.part.SidedPart
import com.kneelawk.wiredredstone.util.RotationUtils
import com.kneelawk.wiredredstone.util.getSidedPart
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

    override fun findConnections(ctx: NodeContext): MutableCollection<NodeHolder<BlockNode>> {
        return WireConnectionDiscoverers.wireFindConnections(this, ctx, filter)
    }

    override fun canConnect(ctx: NodeContext, other: NodeHolder<BlockNode>): Boolean {
        return WireConnectionDiscoverers.wireCanConnect(this, ctx, other, filter)
    }

    override fun canConnect(
        ctx: NodeContext, inDirection: Direction, connectionType: WireConnectionType, other: NodeHolder<BlockNode>
    ): Boolean {
        val part = partClass.safeCast(ctx.getSidedPart<AbstractGatePart>()) ?: return false

        val cardinal = getConnectDirection(part)

        return RotationUtils.rotatedDirection(side, cardinal) == inDirection
    }

    override fun onConnectionsChanged(ctx: NodeContext) {
        RedstoneLogic.scheduleUpdate(ctx.blockWorld, ctx.pos)
        ctx.getSidedPart<AbstractGatePart>()?.updateConnections()
    }
}
