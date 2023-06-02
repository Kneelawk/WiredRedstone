package com.kneelawk.wiredredstone.node

import com.kneelawk.graphlib.api.graph.NodeContext
import com.kneelawk.graphlib.api.graph.user.BlockNodeDecoder
import com.kneelawk.graphlib.api.util.HalfLink
import com.kneelawk.graphlib.api.util.SidedPos
import com.kneelawk.graphlib.api.wire.SidedWireBlockNode
import com.kneelawk.graphlib.api.wire.WireConnectionDiscoverers
import com.kneelawk.wiredredstone.logic.RedstoneCarrierFilter
import com.kneelawk.wiredredstone.logic.RedstoneLogic
import com.kneelawk.wiredredstone.logic.RedstoneWireType
import com.kneelawk.wiredredstone.part.RedAlloyWirePart
import com.kneelawk.wiredredstone.part.SidedPart
import com.kneelawk.wiredredstone.util.NetNode
import com.kneelawk.wiredredstone.util.connectable.WireBlockageFilter
import com.kneelawk.wiredredstone.util.getSidedPart
import net.minecraft.nbt.NbtByte
import net.minecraft.nbt.NbtElement
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockView

data class RedAlloyWireBlockNode(private val side: Direction) : SidedWireBlockNode, RedstoneCarrierBlockNode {

    private val filter =
        RedstoneCarrierFilter.and(WireBlockageFilter(side, RedAlloyWirePart.WIRE_WIDTH, RedAlloyWirePart.WIRE_HEIGHT))

    override val redstoneType = RedstoneWireType.RedAlloy

    override fun getSide(): Direction = side
    override fun getTypeId(): Identifier = WRBlockNodes.RED_ALLOY_WIRE_ID

    private fun getPart(world: BlockView, pos: BlockPos): RedAlloyWirePart? {
        return SidedPart.getPart(world, SidedPos(pos, side))
    }

    override fun findConnections(ctx: NodeContext): MutableCollection<HalfLink> {
        return WireConnectionDiscoverers.wireFindConnections(this, ctx, filter)
    }

    override fun canConnect(ctx: NodeContext, link: HalfLink): Boolean {
        return WireConnectionDiscoverers.wireCanConnect(this, ctx, link, filter)
    }

    override fun putPower(world: ServerWorld, self: NetNode, power: Int) {
        val part = getPart(world, self.pos) ?: return
        // Updating neighbors is handled by updatePowered()
        part.updatePower(power)
        part.redraw()
    }

    override fun sourcePower(world: ServerWorld, self: NetNode): Int {
        val part = getPart(world, self.pos) ?: return 0
        val pos = SidedPos(self.pos, side)
        return RedstoneLogic.getReceivingPower(world, pos, part.connections, true, part.blockage)
    }

    override fun onConnectionsChanged(ctx: NodeContext) {
        RedstoneLogic.scheduleUpdate(ctx.blockWorld, ctx.pos)
        ctx.getSidedPart<RedAlloyWirePart>()?.updateInternalConnections(ctx.blockWorld)
    }

    override fun toTag(): NbtElement? {
        return NbtByte.of(side.id.toByte())
    }

    object Decoder : BlockNodeDecoder {
        override fun decode(tag: NbtElement?): RedAlloyWireBlockNode? {
            val byte = tag as? NbtByte ?: return null
            return RedAlloyWireBlockNode(Direction.byId(byte.intValue()))
        }
    }
}
