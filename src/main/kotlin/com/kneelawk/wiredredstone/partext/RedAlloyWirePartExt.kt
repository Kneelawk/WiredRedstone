package com.kneelawk.wiredredstone.partext

import com.kneelawk.wiredredstone.part.AbstractSidedPart
import com.kneelawk.wiredredstone.part.RedAlloyWirePart
import com.kneelawk.wiredredstone.part.SidedPart
import com.kneelawk.wiredredstone.util.*
import com.kneelawk.wiredredstone.wirenet.*
import com.kneelawk.wiredredstone.wirenet.conn.ConnectionDiscoverers
import com.kneelawk.wiredredstone.wirenet.conn.find
import net.minecraft.nbt.NbtByte
import net.minecraft.nbt.NbtElement
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockView
import net.minecraft.world.World

data class RedAlloyWirePartExt(override val side: Direction) : ConnectablePartExt, RedstoneCarrierPartExt {
    override val type = Type

    override val redstoneType = RedstoneWireType.RedAlloy

    override fun tryConnect(self: NetNode, world: ServerWorld, pos: BlockPos, nv: NodeView): Set<NetNode> {
        return find(ConnectionDiscoverers.WIRE, RedstoneCarrierFilter, self, world, pos, nv)
    }

    override fun getState(world: World, self: NetNode): Boolean {
        val part = SidedPart.getPart(world, SidedPos(self.data.pos, side)) as? RedAlloyWirePart ?: return false
        return part.powered
    }

    override fun setState(world: World, self: NetNode, state: Boolean) {
        val part = SidedPart.getPart(world, SidedPos(self.data.pos, side)) as? RedAlloyWirePart ?: return
        // Updating neighbors is handled by updatePowered()
        part.updatePowered(state)
        part.redraw()
    }

    override fun getInput(world: World, self: NetNode): Boolean {
        val pos = SidedPos(self.data.pos, side)
        val part = SidedPart.getPart(world, pos) as? RedAlloyWirePart ?: return false
        return RedstoneLogic.isReceivingPower(world, pos, part.connections, true, part.blockage)
    }

    override fun onChanged(self: NetNode, world: ServerWorld, pos: BlockPos) {
        RedstoneLogic.scheduleUpdate(world, pos)
        ConnectableUtils.updateBlockageAndConnections(
            world, SidedPos(pos, side), RedAlloyWirePart.WIRE_WIDTH, RedAlloyWirePart.WIRE_HEIGHT
        )
    }

    override fun toTag(): NbtElement? {
        return NbtByte.of(side.id.toByte())
    }

    override fun canConnectAt(world: BlockView, pos: BlockPos, inDirection: Direction, type: ConnectionType): Boolean {
        return ConnectableUtils.canWireConnect(world, pos, inDirection, type, side, 2.0, 2.0)
    }

    object Type : SidedPartExtType {
        override fun createExtFromTag(tag: NbtElement?): PartExt? {
            val byte = tag as? NbtByte ?: return null
            return RedAlloyWirePartExt(Direction.byId(byte.intValue()))
        }

        override fun createExtsForPart(world: World, pos: SidedPos, part: AbstractSidedPart): Set<PartExt> {
            return setOf(RedAlloyWirePartExt(pos.side))
        }
    }
}