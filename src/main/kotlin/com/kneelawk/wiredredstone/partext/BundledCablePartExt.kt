package com.kneelawk.wiredredstone.partext

import com.kneelawk.wiredredstone.part.BundledCablePart
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

data class BundledCablePartExt(override val side: Direction, val color: DyeColor?, val inner: DyeColor) :
    ConnectablePartExt, RedstoneCarrierPartExt {
    override val type = Type

    override val redstoneType = RedstoneWireType.Bundled(color, inner)

    private fun getPart(world: BlockView, pos: BlockPos): BundledCablePart? {
        return SidedPart.getPart(world, SidedPos(pos, side)) as? BundledCablePart
    }

    override fun tryConnect(self: NetNode, world: ServerWorld, pos: BlockPos, nv: NodeView): Set<NetNode> {
        return find(ConnectionDiscoverers.WIRE, RedstoneCarrierFilter, self, world, pos, nv)
    }

    override fun getState(world: World, self: NetNode): Int {
        return 0
    }

    override fun setState(world: World, self: NetNode, state: Int) {
    }

    override fun getInput(world: World, self: NetNode): Int {
        // TODO: BundledCableIO support
        return 0
    }

    override fun onChanged(self: NetNode, world: ServerWorld, pos: BlockPos) {
        getPart(world, pos)?.handleUpdates()
    }

    override fun canConnectAt(world: BlockView, pos: BlockPos, inDirection: Direction, type: ConnectionType): Boolean {
        return ConnectableUtils.canWireConnect(
            world, pos, inDirection, type, side, BundledCablePart.WIRE_WIDTH, BundledCablePart.WIRE_HEIGHT
        )
    }

    override fun toTag(): NbtElement {
        val tag = NbtCompound()
        tag.putByte("side", side.id.toByte())
        color?.let { tag.putByte("color", it.id.toByte()) }
        tag.putByte("inner", inner.id.toByte())
        return tag
    }

    object Type : SidedPartExtType {
        override fun createExtFromTag(tag: NbtElement?): PartExt? {
            if (tag !is NbtCompound) return null
            val side = Direction.byId(tag.getByte("side").toInt())
            val color = if (tag.contains("color")) DyeColor.byId(tag.getByte("color").toInt()) else null
            val inner = DyeColor.byId(tag.getByte("inner").toInt())
            return BundledCablePartExt(side, color, inner)
        }

        override fun createExtsForPart(world: World, pos: SidedPos, part: SidedPart): Set<PartExt> {
            if (part !is BundledCablePart) return setOf()
            return DyeColor.values().asSequence().map { BundledCablePartExt(part.side, part.color, it) }.toSet()
        }
    }
}
