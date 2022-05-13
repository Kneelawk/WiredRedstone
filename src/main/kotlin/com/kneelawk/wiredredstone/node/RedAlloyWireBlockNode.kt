package com.kneelawk.wiredredstone.node

import com.kneelawk.graphlib.graph.BlockNode
import com.kneelawk.graphlib.graph.BlockNodeDecoder
import com.kneelawk.graphlib.graph.NodeView
import com.kneelawk.graphlib.util.SidedPos
import com.kneelawk.graphlib.wire.SidedWireBlockNode
import com.kneelawk.graphlib.wire.WireConnectionDiscoverers
import com.kneelawk.graphlib.wire.WireConnectionType
import com.kneelawk.wiredredstone.part.RedAlloyWirePart
import com.kneelawk.wiredredstone.part.SidedPart
import com.kneelawk.wiredredstone.util.*
import com.kneelawk.wiredredstone.util.NetNode
import net.minecraft.nbt.NbtByte
import net.minecraft.nbt.NbtElement
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockView
import net.minecraft.world.World

data class RedAlloyWireBlockNode(private val side: Direction) : SidedWireBlockNode, RedstoneCarrierBlockNode {
    override val redstoneType = RedstoneWireType.RedAlloy

    override fun getSide(): Direction = side
    override fun getTypeId(): Identifier = WRBlockNodes.RED_ALLOY_WIRE_ID

    private fun getPart(world: BlockView, pos: BlockPos): RedAlloyWirePart? {
        return SidedPart.getPart(world, SidedPos(pos, side)) as? RedAlloyWirePart
    }

    override fun findConnections(world: ServerWorld, nv: NodeView, pos: BlockPos): Collection<NetNode> {
        return WireConnectionDiscoverers.wireFindConnections(this, world, nv, pos, RedstoneCarrierFilter)
    }

    override fun canConnect(world: ServerWorld, nodeView: NodeView, pos: BlockPos, other: NetNode): Boolean {
        return WireConnectionDiscoverers.wireCanConnect(this, world, pos, RedstoneCarrierFilter, other)
    }

    override fun getState(world: World, self: NetNode): Int {
        val part = getPart(world, self.pos) ?: return 0
        return part.power
    }

    override fun setState(world: World, self: NetNode, state: Int) {
        val part = getPart(world, self.pos) ?: return
        // Updating neighbors is handled by updatePowered()
        part.updatePower(state)
        part.redraw()
    }

    override fun getInput(world: World, self: NetNode): Int {
        val part = getPart(world, self.pos) ?: return 0
        val pos = SidedPos(self.pos, side)
        return RedstoneLogic.getReceivingPower(world, pos, part.connections, true, part.blockage)
    }

    override fun onChanged(world: ServerWorld, pos: BlockPos) {
        RedstoneLogic.scheduleUpdate(world, pos)
        getPart(world, pos)?.updateConnections()
    }

    override fun toTag(): NbtElement? {
        return NbtByte.of(side.id.toByte())
    }

    override fun canConnect(
        world: ServerWorld, pos: BlockPos, inDirection: Direction, type: WireConnectionType, other: NetNode
    ): Boolean {
        return ConnectableUtils.canWireConnect(
            world, pos, inDirection, type, side, RedAlloyWirePart.WIRE_WIDTH, RedAlloyWirePart.WIRE_HEIGHT
        )
    }

    object Decoder : BlockNodeDecoder {
        override fun createBlockNodeFromTag(tag: NbtElement?): BlockNode? {
            val byte = tag as? NbtByte ?: return null
            return RedAlloyWireBlockNode(Direction.byId(byte.intValue()))
        }
    }
}