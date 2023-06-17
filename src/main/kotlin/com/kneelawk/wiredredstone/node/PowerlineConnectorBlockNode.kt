package com.kneelawk.wiredredstone.node

import alexiil.mc.lib.net.IMsgReadCtx
import alexiil.mc.lib.net.IMsgWriteCtx
import alexiil.mc.lib.net.NetByteBuf
import com.kneelawk.graphlib.api.graph.NodeHolder
import com.kneelawk.graphlib.api.graph.user.*
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
import net.minecraft.util.math.Direction

data class PowerlineConnectorBlockNode(private val side: Direction) : SidedBlockNode, CenterWireBlockNode,
    RedstoneCarrierBlockNode {
    override fun getType(): BlockNodeType = WRBlockNodes.POWERLINE_CONNECTOR

    override fun toTag(): NbtElement = NbtByte.of(side.id.toByte())

    override fun toPacket(buf: NetByteBuf, ctx: IMsgWriteCtx) {
        buf.writeFixedBits(side.id, 3)
    }

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
        val world = ctx.blockWorld
        if (world is ServerWorld) {
            RedstoneLogic.scheduleUpdate(world, ctx.graphId)
        }
    }

    override fun isValid(self: NodeHolder<BlockNode>): Boolean {
        return self.getSidedPart<PowerlineConnectorPart>() != null
    }

    override val redstoneType = RedstoneWireType.RedAlloy

    override fun putPower(world: ServerWorld, self: NodeHolder<RedstoneCarrierBlockNode>, power: Int) = Unit

    override fun sourcePower(world: ServerWorld, self: NodeHolder<RedstoneCarrierBlockNode>): Int = 0

    object Decoder : BlockNodeDecoder, BlockNodePacketDecoder {
        override fun decode(tag: NbtElement?): BlockNode? {
            val byte = tag as? NbtByte ?: return null
            return PowerlineConnectorBlockNode(Direction.byId(byte.intValue()))
        }

        override fun decode(buf: NetByteBuf, ctx: IMsgReadCtx): BlockNode {
            return PowerlineConnectorBlockNode(Direction.byId(buf.readFixedBits(3)))
        }
    }
}
