package com.kneelawk.wiredredstone.partext

import com.kneelawk.wiredredstone.part.InsulatedWirePart
import com.kneelawk.wiredredstone.part.SidedPart
import com.kneelawk.wiredredstone.util.*
import com.kneelawk.wiredredstone.wirenet.*
import com.kneelawk.wiredredstone.wirenet.conn.ConnectionDiscoverers
import com.kneelawk.wiredredstone.wirenet.conn.find
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.DyeColor
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockView
import net.minecraft.world.World

data class InsulatedWirePartExt(override val side: Direction, val color: DyeColor) : ConnectablePartExt,
    RedstoneCarrierPartExt {
    override val type = Type

    override val redstoneType = RedstoneWireType.Colored(color)

    private fun getPart(world: BlockView, pos: BlockPos): InsulatedWirePart? {
        return SidedPart.getPart(world, SidedPos(pos, side)) as? InsulatedWirePart
    }

    override fun tryConnect(self: NetNode, world: ServerWorld, pos: BlockPos, nv: NodeView): Set<NetNode> {
        return find(ConnectionDiscoverers.WIRE, RedstoneCarrierFilter, self, world, pos, nv)
    }

    override fun getState(world: World, self: NetNode): Int {
        val part = getPart(world, self.data.pos) ?: return 0
        return part.power
    }

    override fun setState(world: World, self: NetNode, state: Int) {
        val part = getPart(world, self.data.pos) ?: return
        part.updatePower(state)
    }

    override fun getInput(world: World, self: NetNode): Int {
        val part = getPart(world, self.data.pos) ?: return 0
        val pos = SidedPos(self.data.pos, side)
        return RedstoneLogic.getReceivingPower(world, pos, part.connections, false, part.blockage)
    }

    override fun onChanged(self: NetNode, world: ServerWorld, pos: BlockPos) {
        RedstoneLogic.scheduleUpdate(world, pos)
        getPart(world, pos)?.updateConnections()
    }

    override fun canConnectAt(world: BlockView, pos: BlockPos, inDirection: Direction, type: ConnectionType): Boolean {
        return ConnectableUtils.canWireConnect(
            world, pos, inDirection, type, side, InsulatedWirePart.WIRE_WIDTH, InsulatedWirePart.WIRE_HEIGHT
        )
    }

    override fun toTag(): NbtElement {
        val tag = NbtCompound()
        tag.putByte("side", side.id.toByte())
        tag.putByte("color", color.id.toByte())
        return tag
    }

    object Type : SidedPartExtType {
        override fun createExtFromTag(tag: NbtElement?): PartExt? {
            if (tag !is NbtCompound) return null

            val side = Direction.byId(tag.getByte("side").toInt())
            val color = DyeColor.byId(tag.getByte("color").toInt())
            return InsulatedWirePartExt(side, color)
        }

        override fun createExtsForPart(world: World, pos: SidedPos, part: SidedPart): Set<PartExt> {
            if (part !is InsulatedWirePart) return setOf()
            return setOf(InsulatedWirePartExt(part.side, part.color))
        }
    }
}