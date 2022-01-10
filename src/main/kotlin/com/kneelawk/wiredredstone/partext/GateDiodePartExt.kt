package com.kneelawk.wiredredstone.partext

import com.kneelawk.wiredredstone.WRLog
import com.kneelawk.wiredredstone.part.GateDiodePart
import com.kneelawk.wiredredstone.part.SidedPart
import com.kneelawk.wiredredstone.util.*
import com.kneelawk.wiredredstone.wirenet.*
import com.kneelawk.wiredredstone.wirenet.conn.ConnectionDiscoverers
import com.kneelawk.wiredredstone.wirenet.conn.find
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockView
import net.minecraft.world.World

sealed class GateDiodePartExt(override val side: Direction) : ConnectablePartExt, RedstoneCarrierPartExt {
    override val type = Type
    override val redstoneType = RedstoneWireType.RedAlloy

    protected abstract val typeByte: Byte

    protected abstract fun getConnectDirection(part: GateDiodePart): Direction

    protected fun getPart(world: BlockView, pos: BlockPos): GateDiodePart? {
        return SidedPart.getPart(world, SidedPos(pos, side)) as? GateDiodePart
    }

    override fun toTag(): NbtElement? {
        val tag = NbtCompound()
        tag.putByte("side", side.id.toByte())
        tag.putByte("type", typeByte)
        return tag
    }

    override fun tryConnect(self: NetNode, world: ServerWorld, pos: BlockPos, nv: NodeView): Set<NetNode> {
        return find(ConnectionDiscoverers.WIRE, RedstoneCarrierFilter, self, world, pos, nv)
    }

    override fun canConnectAt(world: BlockView, pos: BlockPos, inDirection: Direction, type: ConnectionType): Boolean {
        val part = getPart(world, pos) ?: return false

        val cardinal = getConnectDirection(part)

        val edge = RotationUtils.rotatedDirection(side, cardinal)
        if (edge != inDirection) {
            return false
        }

        return if (type == ConnectionType.CORNER) {
            val corner = BoundingBoxUtils.getWireOutsideConnectionShape(
                side, cardinal, GateDiodePart.CONNECTION_WIDTH, GateDiodePart.CONNECTION_HEIGHT, true
            ) ?: return true
            !ConnectableUtils.checkOutside(world, pos.offset(inDirection), corner)
        } else true
    }

    override fun onChanged(self: NetNode, world: ServerWorld, pos: BlockPos) {
        RedstoneLogic.scheduleUpdate(world, pos)
        getPart(world, pos)?.updateConnections()
    }

    data class Input(override val side: Direction) : GateDiodePartExt(side) {
        companion object {
            // This is just to reduce the likelihood of hash collisions with Output
            private const val HASH_SALT = -807492579

            const val TYPE_BYTE = 0.toByte()
        }

        override val typeByte = TYPE_BYTE

        override fun getConnectDirection(part: GateDiodePart): Direction {
            return part.getInputSide()
        }

        override fun getState(world: World, self: NetNode): Int {
            return 0
        }

        override fun setState(world: World, self: NetNode, state: Int) {
            getPart(world, self.data.pos)?.updateInputPower(state)
        }

        override fun getInput(world: World, self: NetNode): Int {
            val pos = self.data.pos
            val part = getPart(world, pos) ?: return 0
            val input = part.calculateInputPower()

            // Even though this gate's input does not output any signal to anything else in the network,
            // the gate's input itself is a network of one node, meaning that what's returned here gets
            // sent to setState anyways.
            return input
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Input

            if (side != other.side) return false

            return true
        }

        override fun hashCode(): Int {
            return side.hashCode() xor HASH_SALT
        }
    }

    data class Output(override val side: Direction) : GateDiodePartExt(side) {
        companion object {
            // This is just to reduce the likelihood of hash collisions with Input
            private const val HASH_SALT = 1863451528
        }

        override val typeByte = 1.toByte()

        override fun getConnectDirection(part: GateDiodePart): Direction {
            return part.getOutputSide()
        }

        override fun getState(world: World, self: NetNode): Int {
            return getPart(world, self.data.pos)?.outputPower ?: 0
        }

        override fun setState(world: World, self: NetNode, state: Int) {
            // Nothing to do here. The output does not accept power from the network.
        }

        override fun getInput(world: World, self: NetNode): Int {
            // This is asking about input to the network, so we return our output value.
            return getPart(world, self.data.pos)?.outputPower ?: 0
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Output

            if (side != other.side) return false

            return true
        }

        override fun hashCode(): Int {
            return side.hashCode() xor HASH_SALT
        }
    }

    object Type : SidedPartExtType {
        override fun createExtFromTag(tag: NbtElement?): PartExt? {
            if (tag !is NbtCompound) {
                WRLog.warn("tag is not a compound tag")
                return null
            }

            val side = Direction.byId((tag.maybeGetByte("side") ?: run {
                WRLog.warn("missing 'side' tag")
                return null
            }).toInt())

            val type = (tag.maybeGetByte("type") ?: run {
                WRLog.warn("missing 'type' tag")
                return null
            }).coerceIn(0, 1)

            return if (type == Input.TYPE_BYTE) {
                Input(side)
            } else {
                Output(side)
            }
        }

        override fun createExtsForPart(world: World, pos: SidedPos, part: SidedPart): Set<PartExt> {
            return setOf(Input(part.side), Output(part.side))
        }
    }
}