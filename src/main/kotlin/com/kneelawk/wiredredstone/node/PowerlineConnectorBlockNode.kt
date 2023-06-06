package com.kneelawk.wiredredstone.node

import com.kneelawk.graphlib.api.graph.NodeHolder
import com.kneelawk.graphlib.api.graph.user.BlockNode
import com.kneelawk.graphlib.api.graph.user.BlockNodeDecoder
import com.kneelawk.graphlib.api.graph.user.SidedBlockNode
import com.kneelawk.graphlib.api.util.HalfLink
import com.kneelawk.graphlib.api.wire.CenterWireBlockNode
import com.kneelawk.graphlib.api.wire.WireConnectionDiscoverers
import com.kneelawk.wiredredstone.logic.RedstoneCarrierFilter
import com.kneelawk.wiredredstone.logic.RedstoneLogic
import com.kneelawk.wiredredstone.logic.RedstoneWireType
import com.kneelawk.wiredredstone.part.PowerlineConnectorPart
import com.kneelawk.wiredredstone.util.getSidedPart
import net.minecraft.nbt.NbtByte
import net.minecraft.nbt.NbtElement
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.minecraft.util.math.Direction

data class PowerlineConnectorBlockNode(private val side: Direction) : SidedBlockNode, CenterWireBlockNode, RedstoneCarrierBlockNode {
    override fun getTypeId(): Identifier = WRBlockNodes.POWERLINE_CONNECTOR

    override fun toTag(): NbtElement = NbtByte.of(side.id.toByte())

    override fun getSide(): Direction = side

    override fun findConnections(ctx: NodeHolder<BlockNode>): Collection<HalfLink> {
        return WireConnectionDiscoverers.centerWireFindConnections(this, ctx, RedstoneCarrierFilter)
    }

    override fun canConnect(ctx: NodeHolder<BlockNode>, other: HalfLink): Boolean {
        return WireConnectionDiscoverers.centerWireCanConnect(this, ctx, other, RedstoneCarrierFilter)
    }

    override fun canConnect(ctx: NodeHolder<BlockNode>, onSide: Direction, link: HalfLink): Boolean {
        return onSide == side && link.other.node is SidedBlockNode
    }

    override fun onConnectionsChanged(ctx: NodeHolder<BlockNode>) {
        RedstoneLogic.scheduleUpdate(ctx.blockWorld, ctx.graphId)
    }

    override fun isValid(self: NodeHolder<BlockNode>): Boolean {
        return self.getSidedPart<PowerlineConnectorPart>() != null
    }

    override val redstoneType = RedstoneWireType.RedAlloy

    override fun putPower(world: ServerWorld, self: NodeHolder<RedstoneCarrierBlockNode>, power: Int) = Unit

    override fun sourcePower(world: ServerWorld, self: NodeHolder<RedstoneCarrierBlockNode>): Int = 0

    object Decoder : BlockNodeDecoder {
        override fun decode(tag: NbtElement?): BlockNode? {
            val byte = tag as? NbtByte ?: return null
            return PowerlineConnectorBlockNode(Direction.byId(byte.intValue()))
        }
    }
}
