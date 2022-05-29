package com.kneelawk.wiredredstone.node

import com.kneelawk.graphlib.graph.BlockNode
import com.kneelawk.graphlib.graph.BlockNodeDecoder
import com.kneelawk.graphlib.graph.NodeView
import com.kneelawk.graphlib.util.SidedPos
import com.kneelawk.graphlib.wire.SidedWireBlockNode
import com.kneelawk.graphlib.wire.WireConnectionDiscoverers
import com.kneelawk.wiredredstone.part.BundledCablePart
import com.kneelawk.wiredredstone.part.SidedPart
import com.kneelawk.wiredredstone.util.*
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
        return SidedPart.getPart(world, SidedPos(pos, side)) as? BundledCablePart
    }

    override fun findConnections(world: ServerWorld, nv: NodeView, pos: BlockPos, self: NetNode): Collection<NetNode> {
        return WireConnectionDiscoverers.wireFindConnections(this, world, nv, pos, self, filter)
    }

    override fun canConnect(
        world: ServerWorld, nodeView: NodeView, pos: BlockPos, self: NetNode, other: NetNode
    ): Boolean {
        return WireConnectionDiscoverers.wireCanConnect(this, world, pos, self, other, filter)
    }

    override fun getState(world: ServerWorld, self: NetNode): Int {
        return getPart(world, self.pos)?.getPower(inner) ?: 0
    }

    override fun setState(world: ServerWorld, self: NetNode, state: Int) {
        getPart(world, self.pos)?.updatePower(inner, state)
    }

    override fun getInput(world: ServerWorld, self: NetNode): Int {
        val part = getPart(world, self.pos) ?: return 0
        return BundledCableUtils.getBundledCableInput(
            world, SidedPos(self.pos, side), inner, part.connections, part.blockage
        )
    }

    override fun onConnectionsChanged(world: ServerWorld, pos: BlockPos, self: NetNode) {
        getPart(world, pos)?.handleUpdates()
    }

    override fun toTag(): NbtElement {
        val tag = NbtCompound()
        tag.putByte("side", side.id.toByte())
        color?.let { tag.putByte("color", it.id.toByte()) }
        tag.putByte("inner", inner.id.toByte())
        return tag
    }

    object Decoder : BlockNodeDecoder {
        override fun createBlockNodeFromTag(tag: NbtElement?): BlockNode? {
            if (tag !is NbtCompound) return null
            val side = Direction.byId(tag.getByte("side").toInt())
            val color = if (tag.contains("color")) DyeColor.byId(tag.getByte("color").toInt()) else null
            val inner = DyeColor.byId(tag.getByte("inner").toInt())
            return BundledCableBlockNode(side, color, inner)
        }
    }
}
