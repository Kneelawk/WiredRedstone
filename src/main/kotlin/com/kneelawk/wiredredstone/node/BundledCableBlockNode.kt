package com.kneelawk.wiredredstone.node

import com.kneelawk.graphlib.api.graph.NodeContext
import com.kneelawk.graphlib.api.graph.NodeHolder
import com.kneelawk.graphlib.api.graph.user.BlockNode
import com.kneelawk.graphlib.api.graph.user.BlockNodeDecoder
import com.kneelawk.graphlib.api.util.HalfLink
import com.kneelawk.graphlib.api.util.SidedPos
import com.kneelawk.graphlib.api.wire.SidedWireBlockNode
import com.kneelawk.graphlib.api.wire.WireConnectionDiscoverers
import com.kneelawk.wiredredstone.logic.BundledCableLogic
import com.kneelawk.wiredredstone.logic.RedstoneCarrierFilter
import com.kneelawk.wiredredstone.logic.RedstoneWireType
import com.kneelawk.wiredredstone.part.BundledCablePart
import com.kneelawk.wiredredstone.part.SidedPart
import com.kneelawk.wiredredstone.util.NetNode
import com.kneelawk.wiredredstone.util.connectable.WireBlockageFilter
import com.kneelawk.wiredredstone.util.getSidedPart
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.DyeColor
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockView

data class BundledCableBlockNode(private val side: Direction, val color: DyeColor?, val inner: DyeColor) :
    SidedWireBlockNode, RedstoneCarrierBlockNode {

    private val filter =
        RedstoneCarrierFilter.and(WireBlockageFilter(side, BundledCablePart.WIRE_WIDTH, BundledCablePart.WIRE_HEIGHT))

    override val redstoneType = RedstoneWireType.Bundled(color, inner)

    override fun getSide(): Direction = side
    override fun getTypeId(): Identifier = WRBlockNodes.BUNDLED_CABLE_ID

    private fun getPart(world: BlockView, pos: BlockPos): BundledCablePart? {
        return SidedPart.getPart(world, SidedPos(pos, side))
    }

    override fun findConnections(ctx: NodeContext): MutableCollection<HalfLink> {
        return WireConnectionDiscoverers.wireFindConnections(this, ctx, filter)
    }

    override fun canConnect(ctx: NodeContext, other: HalfLink): Boolean {
        return WireConnectionDiscoverers.wireCanConnect(this, ctx, other, filter)
    }

    override fun putPower(world: ServerWorld, self: NetNode, power: Int) {
        getPart(world, self.pos)?.updatePower(inner, power)
    }

    override fun sourcePower(world: ServerWorld, self: NetNode): Int {
        val part = getPart(world, self.pos) ?: return 0
        return BundledCableLogic.getBundledCableInput(
            world, SidedPos(self.pos, side), inner, part.connections, part.blockage
        )
    }

    override fun onConnectionsChanged(ctx: NodeContext) {
        ctx.getSidedPart<BundledCablePart>()?.handleUpdates()
    }

    override fun toTag(): NbtElement {
        val tag = NbtCompound()
        tag.putByte("side", side.id.toByte())
        color?.let { tag.putByte("color", it.id.toByte()) }
        tag.putByte("inner", inner.id.toByte())
        return tag
    }

    object Decoder : BlockNodeDecoder {
        override fun decode(tag: NbtElement?): BundledCableBlockNode? {
            if (tag !is NbtCompound) return null
            val side = Direction.byId(tag.getByte("side").toInt())
            val color = if (tag.contains("color")) DyeColor.byId(tag.getByte("color").toInt()) else null
            val inner = DyeColor.byId(tag.getByte("inner").toInt())
            return BundledCableBlockNode(side, color, inner)
        }
    }
}
