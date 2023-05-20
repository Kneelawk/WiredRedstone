package com.kneelawk.wiredredstone.node

import com.kneelawk.graphlib.api.graph.GraphView
import com.kneelawk.graphlib.api.graph.NodeHolder
import com.kneelawk.graphlib.api.node.BlockNode
import com.kneelawk.graphlib.api.node.BlockNodeDecoder
import com.kneelawk.graphlib.api.node.KeyBlockNode
import com.kneelawk.graphlib.api.node.NodeKeyExtra
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
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.DyeColor
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockView

data class BundledCableBlockNode(private val side: Direction, val color: DyeColor?, val inner: DyeColor) :
    SidedWireBlockNode, RedstoneCarrierBlockNode, KeyBlockNode {

    private val filter =
        RedstoneCarrierFilter.and(WireBlockageFilter(side, BundledCablePart.WIRE_WIDTH, BundledCablePart.WIRE_HEIGHT))

    override val redstoneType = RedstoneWireType.Bundled(color, inner)

    override fun getSide(): Direction = side
    override fun getTypeId(): Identifier = WRBlockNodes.BUNDLED_CABLE_ID

    private fun getPart(world: BlockView, pos: BlockPos): BundledCablePart? {
        return SidedPart.getPart(world, SidedPos(pos, side)) as? BundledCablePart
    }

    override fun findConnections(
        self: NodeHolder<BlockNode>, world: ServerWorld, nv: GraphView
    ): MutableCollection<NodeHolder<BlockNode>> {
        return WireConnectionDiscoverers.wireFindConnections(this, self, world, nv, filter)
    }

    override fun canConnect(
        self: NodeHolder<BlockNode>, world: ServerWorld, nodeView: GraphView, other: NodeHolder<BlockNode>
    ): Boolean {
        return WireConnectionDiscoverers.wireCanConnect(this, self, world, other, filter)
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

    override fun onConnectionsChanged(self: NodeHolder<BlockNode>, world: ServerWorld, gv: GraphView) {
        getPart(world, self.pos)?.handleUpdates()
    }

    override fun toTag(): NbtElement {
        val tag = NbtCompound()
        tag.putByte("side", side.id.toByte())
        color?.let { tag.putByte("color", it.id.toByte()) }
        tag.putByte("inner", inner.id.toByte())
        return tag
    }

    override fun getKeyExtra(): NodeKeyExtra = this

    object Decoder : BlockNodeDecoder {
        override fun createBlockNodeFromTag(tag: NbtElement?): BundledCableBlockNode? {
            if (tag !is NbtCompound) return null
            val side = Direction.byId(tag.getByte("side").toInt())
            val color = if (tag.contains("color")) DyeColor.byId(tag.getByte("color").toInt()) else null
            val inner = DyeColor.byId(tag.getByte("inner").toInt())
            return BundledCableBlockNode(side, color, inner)
        }

        override fun createKeyExtraFromTag(tag: NbtElement?): NodeKeyExtra? {
            return createBlockNodeFromTag(tag)
        }
    }
}
