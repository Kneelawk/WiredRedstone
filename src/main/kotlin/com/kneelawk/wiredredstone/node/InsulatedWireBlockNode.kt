package com.kneelawk.wiredredstone.node

import alexiil.mc.lib.multipart.api.AbstractPart
import com.kneelawk.graphlib.api.graph.NodeHolder
import com.kneelawk.graphlib.api.graph.user.BlockNode
import com.kneelawk.graphlib.api.graph.user.BlockNodeDecoder
import com.kneelawk.graphlib.api.graph.user.BlockNodeType
import com.kneelawk.graphlib.api.util.HalfLink
import com.kneelawk.graphlib.api.util.SidedPos
import com.kneelawk.graphlib.api.wire.SidedWireBlockNode
import com.kneelawk.graphlib.api.wire.WireConnectionDiscoverers
import com.kneelawk.wiredredstone.logic.RedstoneCarrierFilter
import com.kneelawk.wiredredstone.logic.RedstoneLogic
import com.kneelawk.wiredredstone.logic.RedstoneWireType
import com.kneelawk.wiredredstone.part.InsulatedWirePart
import com.kneelawk.wiredredstone.part.SidedPart
import com.kneelawk.wiredredstone.util.connectable.WireBlockageFilter
import com.kneelawk.wiredredstone.util.getSidedPart
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.DyeColor
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockView

data class InsulatedWireBlockNode(private val side: Direction, val color: DyeColor) : SidedWireBlockNode,
    RedstoneCarrierBlockNode, PartBlockNode {

    private val filter =
        RedstoneCarrierFilter.and(WireBlockageFilter(side, InsulatedWirePart.WIRE_WIDTH, InsulatedWirePart.WIRE_HEIGHT))

    override val redstoneType = RedstoneWireType.Colored(color)

    override fun getSide(): Direction = side
    override fun getType(): BlockNodeType = WRBlockNodes.INSULATED_WIRE

    private fun getPart(world: BlockView, pos: BlockPos): InsulatedWirePart? {
        return SidedPart.getPart(world, SidedPos(pos, side))
    }

    override fun getPart(self: NodeHolder<BlockNode>): AbstractPart? {
        return getPart(self.blockWorld, self.blockPos)
    }

    override fun findConnections(ctx: NodeHolder<BlockNode>): MutableCollection<HalfLink> {
        return WireConnectionDiscoverers.sidedWireFindConnections(this, ctx, filter)
    }

    override fun canConnect(ctx: NodeHolder<BlockNode>, link: HalfLink): Boolean {
        return WireConnectionDiscoverers.sidedWireCanConnect(this, ctx, link, filter)
    }

    override fun putPower(world: ServerWorld, self: NodeHolder<RedstoneCarrierBlockNode>, power: Int) {
        val part = getPart(world, self.blockPos) ?: return
        part.updatePower(power)
        part.redraw()
    }

    override fun sourcePower(world: ServerWorld, self: NodeHolder<RedstoneCarrierBlockNode>): Int {
        val part = getPart(world, self.blockPos) ?: return 0
        val pos = SidedPos(self.blockPos, side)
        return RedstoneLogic.getReceivingPower(world, pos, part.connections, false, part.blockage)
    }

    override fun onConnectionsChanged(ctx: NodeHolder<BlockNode>) {
        val world = ctx.blockWorld
        if (world is ServerWorld) {
            RedstoneLogic.scheduleUpdate(world, ctx.graphId)
            ctx.getSidedPart<InsulatedWirePart>()?.updateInternalConnections(world)
        }
    }

    override fun isValid(self: NodeHolder<BlockNode>): Boolean {
        return self.getSidedPart<InsulatedWirePart>() != null
    }

    override fun toTag(): NbtElement {
        val tag = NbtCompound()
        tag.putByte("side", side.id.toByte())
        tag.putByte("color", color.id.toByte())
        return tag
    }

    object Decoder : BlockNodeDecoder {
        override fun decode(tag: NbtElement?): InsulatedWireBlockNode? {
            if (tag !is NbtCompound) return null

            val side = Direction.byId(tag.getByte("side").toInt())
            val color = DyeColor.byId(tag.getByte("color").toInt())
            return InsulatedWireBlockNode(side, color)
        }
    }
}
