package com.kneelawk.wiredredstone.node

import com.kneelawk.graphlib.graph.BlockNode
import com.kneelawk.graphlib.graph.BlockNodeDecoder
import com.kneelawk.graphlib.graph.BlockNodeWrapper
import com.kneelawk.graphlib.graph.NodeView
import com.kneelawk.graphlib.graph.struct.Node
import com.kneelawk.graphlib.util.SidedPos
import com.kneelawk.graphlib.wire.SidedWireBlockNode
import com.kneelawk.graphlib.wire.WireConnectionDiscoverers
import com.kneelawk.graphlib.wire.WireConnectionType
import com.kneelawk.wiredredstone.WRLog
import com.kneelawk.wiredredstone.part.GateDiodePart
import com.kneelawk.wiredredstone.part.SidedPart
import com.kneelawk.wiredredstone.util.*
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockView
import net.minecraft.world.World
import kotlin.math.max

sealed class GateDiodeBlockNode(private val side: Direction) : SidedWireBlockNode, RedstoneCarrierBlockNode {
    override val redstoneType = RedstoneWireType.RedAlloy

    protected abstract val typeByte: Byte

    override fun getSide(): Direction = side
    override fun getTypeId(): Identifier = WRBlockNodes.GATE_DIODE_ID

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

    override fun findConnections(world: ServerWorld, nv: NodeView, pos: BlockPos): Collection<NetNode> {
        return WireConnectionDiscoverers.wireFindConnections(this, world, nv, pos, RedstoneCarrierFilter)
    }

    override fun canConnect(
        world: ServerWorld, nodeView: NodeView, pos: BlockPos, other: Node<BlockNodeWrapper<*>>
    ): Boolean {
        return WireConnectionDiscoverers.wireCanConnect(this, world, pos, RedstoneCarrierFilter, other)
    }

    override fun canConnect(
        world: ServerWorld, pos: BlockPos, inDirection: Direction, connectionType: WireConnectionType, other: NetNode
    ): Boolean {
        val part = getPart(world, pos) ?: return false

        val cardinal = getConnectDirection(part)

        val edge = RotationUtils.rotatedDirection(side, cardinal)
        if (edge != inDirection) {
            return false
        }

        return if (connectionType == WireConnectionType.CORNER) {
            val corner = BoundingBoxUtils.getWireOutsideConnectionShape(
                side, cardinal, GateDiodePart.CONNECTION_WIDTH, GateDiodePart.CONNECTION_HEIGHT, true
            ) ?: return true
            !ConnectableUtils.checkOutside(world, pos.offset(inDirection), corner)
        } else true
    }

    override fun onChanged(world: ServerWorld, pos: BlockPos) {
        RedstoneLogic.scheduleUpdate(world, pos)
        getPart(world, pos)?.updateConnections()
    }

    data class Input(private val side: Direction) : GateDiodeBlockNode(side) {
        companion object {
            // This is just to reduce the likelihood of hash collisions with Output
            private const val HASH_SALT = -807492579

            const val TYPE_BYTE = 0.toByte()
        }

        override val typeByte = TYPE_BYTE

        override fun getSide(): Direction = side

        override fun getConnectDirection(part: GateDiodePart): Direction {
            return part.getInputSide()
        }

        override fun getState(world: World, self: NetNode): Int {
            return 0
        }

        override fun setState(world: World, self: NetNode, state: Int) {
            getPart(world, self.pos)?.updateInputPower(state)
        }

        override fun getInput(world: World, self: NetNode): Int {
            val pos = self.pos
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

    data class Output(private val side: Direction) : GateDiodeBlockNode(side) {
        companion object {
            // This is just to reduce the likelihood of hash collisions with Input
            private const val HASH_SALT = 1863451528
        }

        override val typeByte = 1.toByte()

        override fun getSide(): Direction = side

        override fun getConnectDirection(part: GateDiodePart): Direction {
            return part.getOutputSide()
        }

        override fun getState(world: World, self: NetNode): Int {
            return getPart(world, self.pos)?.getTotalOutputPower() ?: 0
        }

        override fun setState(world: World, self: NetNode, state: Int) {
            getPart(world, self.pos)?.updateOutputReversePower(state)
        }

        override fun getInput(world: World, self: NetNode): Int {
            val pos = self.pos
            val part = getPart(world, pos) ?: return 0

            // This is asking about input to the network, so we return either our output value or the value calculated
            // by redstone in the world.

            return max(part.outputPower, part.calculateOutputReversePower())
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

    object Decoder : BlockNodeDecoder {
        override fun createBlockNodeFromTag(tag: NbtElement?): BlockNode? {
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
    }
}