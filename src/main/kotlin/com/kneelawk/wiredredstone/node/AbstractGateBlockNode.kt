package com.kneelawk.wiredredstone.node

import alexiil.mc.lib.multipart.api.AbstractPart
import com.kneelawk.graphlib.api.graph.NodeHolder
import com.kneelawk.graphlib.api.graph.user.BlockNode
import com.kneelawk.graphlib.api.util.HalfLink
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
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockView
import kotlin.reflect.KClass
import kotlin.reflect.safeCast

abstract class AbstractGateBlockNode<P : AbstractGatePart>(private val partClass: KClass<P>) : SidedWireBlockNode,
    RedstoneCarrierBlockNode, PartBlockNode {
    protected abstract val filter: SidedWireConnectionFilter

    protected abstract fun getConnectDirection(part: P): Direction

    protected fun getPart(world: BlockView, pos: BlockPos): P? {
        return SidedPart.getPart(world, SidedPos(pos, side), partClass.java)
    }

    override fun findConnections(ctx: NodeHolder<BlockNode>): MutableCollection<HalfLink> {
        return WireConnectionDiscoverers.sidedWireFindConnections(this, ctx, filter)
    }

    override fun canConnect(ctx: NodeHolder<BlockNode>, link: HalfLink): Boolean {
        return WireConnectionDiscoverers.sidedWireCanConnect(this, ctx, link, filter)
    }

    override fun canConnect(
        ctx: NodeHolder<BlockNode>, inDirection: Direction, connectionType: WireConnectionType, link: HalfLink
    ): Boolean {
        val part = partClass.safeCast(ctx.getSidedPart<AbstractGatePart>()) ?: return false

        val cardinal = getConnectDirection(part)

        return RotationUtils.rotatedDirection(side, cardinal) == inDirection
    }

    override fun onConnectionsChanged(ctx: NodeHolder<BlockNode>) {
        val world = ctx.blockWorld
        if (world is ServerWorld) {
            RedstoneLogic.scheduleUpdate(world, ctx.graphId)
            ctx.getSidedPart<AbstractGatePart>()?.updateConnections()
        }
    }

    override fun isValid(self: NodeHolder<BlockNode>): Boolean {
        return self.getSidedPart<AbstractGatePart>() != null
    }

    override fun getPart(self: NodeHolder<BlockNode>): AbstractPart? {
        return self.getSidedPart<AbstractGatePart>()
    }
}
