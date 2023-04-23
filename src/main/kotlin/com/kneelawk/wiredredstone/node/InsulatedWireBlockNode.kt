package com.kneelawk.wiredredstone.node

import com.kneelawk.graphlib.graph.BlockNode
import com.kneelawk.graphlib.graph.BlockNodeDecoder
import com.kneelawk.graphlib.graph.NodeView
import com.kneelawk.graphlib.util.SidedPos
import com.kneelawk.graphlib.wire.SidedWireBlockNode
import com.kneelawk.graphlib.wire.WireConnectionDiscoverers
import com.kneelawk.wiredredstone.logic.RedstoneCarrierFilter
import com.kneelawk.wiredredstone.logic.RedstoneLogic
import com.kneelawk.wiredredstone.logic.RedstoneWireType
import com.kneelawk.wiredredstone.part.InsulatedWirePart
import com.kneelawk.wiredredstone.part.SidedPart
import com.kneelawk.wiredredstone.util.NetNode
import com.kneelawk.wiredredstone.util.connectable.WireBlockageFilter
import com.kneelawk.wiredredstone.util.pos
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.DyeColor
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockView

data class InsulatedWireBlockNode(private val side: Direction, val color: DyeColor) : SidedWireBlockNode,
    RedstoneCarrierBlockNode {

    private val filter =
        RedstoneCarrierFilter.and(WireBlockageFilter(side, InsulatedWirePart.WIRE_WIDTH, InsulatedWirePart.WIRE_HEIGHT))

    override val redstoneType = RedstoneWireType.Colored(color)

    override fun getSide(): Direction = side
    override fun getTypeId(): Identifier = WRBlockNodes.INSULATED_WIRE_ID

    private fun getPart(world: BlockView, pos: BlockPos): InsulatedWirePart? {
        return SidedPart.getPart(world, SidedPos(pos, side)) as? InsulatedWirePart
    }

    override fun findConnections(world: ServerWorld, nv: NodeView, pos: BlockPos, self: NetNode): Collection<NetNode> {
        return WireConnectionDiscoverers.wireFindConnections(this, world, nv, pos, self, filter)
    }

    override fun canConnect(
        world: ServerWorld, nodeView: NodeView, pos: BlockPos, self: NetNode, other: NetNode
    ): Boolean {
        return WireConnectionDiscoverers.wireCanConnect(this, world, pos, self, other, filter)
    }

    override fun putPower(world: ServerWorld, self: NetNode, power: Int) {
        val part = getPart(world, self.pos) ?: return
        part.updatePower(power)
        part.redraw()
    }

    override fun sourcePower(world: ServerWorld, self: NetNode): Int {
        val part = getPart(world, self.pos) ?: return 0
        val pos = SidedPos(self.pos, side)
        return RedstoneLogic.getReceivingPower(world, pos, part.connections, false, part.blockage)
    }

    override fun onConnectionsChanged(world: ServerWorld, pos: BlockPos, self: NetNode) {
        RedstoneLogic.scheduleUpdate(world, pos)
        getPart(world, pos)?.updateConnections(world)
    }

    override fun toTag(): NbtElement {
        val tag = NbtCompound()
        tag.putByte("side", side.id.toByte())
        tag.putByte("color", color.id.toByte())
        return tag
    }

    object Decoder : BlockNodeDecoder {
        override fun createBlockNodeFromTag(tag: NbtElement?): BlockNode? {
            if (tag !is NbtCompound) return null

            val side = Direction.byId(tag.getByte("side").toInt())
            val color = DyeColor.byId(tag.getByte("color").toInt())
            return InsulatedWireBlockNode(side, color)
        }
    }
}
