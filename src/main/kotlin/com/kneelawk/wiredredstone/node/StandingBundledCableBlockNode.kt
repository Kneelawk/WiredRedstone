package com.kneelawk.wiredredstone.node

import alexiil.mc.lib.multipart.api.AbstractPart
import com.kneelawk.graphlib.api.graph.NodeHolder
import com.kneelawk.graphlib.api.graph.user.BlockNode
import com.kneelawk.graphlib.api.graph.user.BlockNodeDecoder
import com.kneelawk.graphlib.api.graph.user.BlockNodeType
import com.kneelawk.graphlib.api.util.HalfLink
import com.kneelawk.graphlib.api.wire.CenterWireBlockNode
import com.kneelawk.graphlib.api.wire.WireConnectionDiscoverers
import com.kneelawk.wiredredstone.logic.BundledCableLogic
import com.kneelawk.wiredredstone.logic.RedstoneCarrierFilter
import com.kneelawk.wiredredstone.logic.RedstoneWireType
import com.kneelawk.wiredredstone.part.StandingBundledCablePart
import com.kneelawk.wiredredstone.util.RedstoneNode
import com.kneelawk.wiredredstone.util.connectable.CenterWireBlockageFilter
import com.kneelawk.wiredredstone.util.getCenterPart
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.DyeColor

data class StandingBundledCableBlockNode(private val color: DyeColor?, private val inner: DyeColor) : CenterWireBlockNode,
    RedstoneCarrierBlockNode, PartBlockNode {
    private val filter = RedstoneCarrierFilter.and(CenterWireBlockageFilter(StandingBundledCablePart.WIRE_DIAMETER))

    override val redstoneType = RedstoneWireType.Bundled(color, inner)

    override fun getType(): BlockNodeType = WRBlockNodes.STANDING_BUNDLED_CABLE

    override fun toTag(): NbtElement {
        val tag = NbtCompound()
        color?.let { tag.putByte("color", it.id.toByte()) }
        tag.putByte("inner", inner.id.toByte())
        return tag
    }

    override fun onConnectionsChanged(self: NodeHolder<BlockNode>) {
        self.getCenterPart<StandingBundledCablePart>()?.handleUpdates()
    }

    override fun findConnections(self: NodeHolder<BlockNode>): MutableCollection<HalfLink> {
        return WireConnectionDiscoverers.centerWireFindConnections(this, self, filter)
    }

    override fun canConnect(self: NodeHolder<BlockNode>, other: HalfLink): Boolean {
        return WireConnectionDiscoverers.centerWireCanConnect(this, self, other, filter)
    }

    override fun isValid(self: NodeHolder<BlockNode>): Boolean {
        return self.getCenterPart<StandingBundledCablePart>() != null
    }

    override fun getPart(self: NodeHolder<BlockNode>): AbstractPart? {
        return self.getCenterPart<StandingBundledCablePart>()
    }

    override fun putPower(world: ServerWorld, self: RedstoneNode, power: Int) {
        self.getCenterPart<StandingBundledCablePart>()?.updatePower(inner, power)
    }

    override fun sourcePower(world: ServerWorld, self: RedstoneNode): Int {
        val part = self.getCenterPart<StandingBundledCablePart>() ?: return 0
        return BundledCableLogic.getCenterBundledCableInput(
            world, self.blockPos, inner, part.connections, part.blockage
        )
    }

    object Decoder : BlockNodeDecoder {
        override fun decode(tag: NbtElement?): BlockNode? {
            if (tag !is NbtCompound) return null

            val color = if (tag.contains("color")) DyeColor.byId(tag.getByte("color").toInt()) else null
            val inner = DyeColor.byId(tag.getByte("inner").toInt())

            return StandingBundledCableBlockNode(color, inner)
        }
    }
}
