package com.kneelawk.wiredredstone.node

import com.kneelawk.graphlib.graph.BlockNode
import com.kneelawk.graphlib.graph.BlockNodeDecoder
import com.kneelawk.graphlib.graph.BlockNodeWrapper
import com.kneelawk.graphlib.graph.NodeView
import com.kneelawk.graphlib.graph.struct.Node
import com.kneelawk.graphlib.util.SidedPos
import com.kneelawk.graphlib.wire.SidedWireBlockNode
import com.kneelawk.graphlib.wire.SidedWireConnectionFilter
import com.kneelawk.graphlib.wire.WireConnectionDiscoverers
import com.kneelawk.graphlib.wire.WireConnectionType
import com.kneelawk.wiredredstone.part.AbstractGatePart
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

sealed class GateDiodeBlockNode : AbstractGateBlockNode<GateDiodePart>(GateDiodePart::class) {
    override val filter: SidedWireConnectionFilter by lazy {
        // must be lazy or this would be initialized before side
        RedstoneCarrierFilter.and(
            WireCornerBlockageFilter(side, AbstractGatePart.CONNECTION_WIDTH, AbstractGatePart.CONNECTION_HEIGHT)
        )
    }

    override val redstoneType = RedstoneWireType.RedAlloy

    protected abstract val type: Type

    override fun getTypeId(): Identifier = WRBlockNodes.GATE_DIODE_ID

    override fun toTag(): NbtElement? = BlockNodeUtil.writeSidedType(side, type)

    data class Input(private val side: Direction) : GateDiodeBlockNode() {
        override val type = Type.INPUT

        override fun getSide(): Direction = side

        override fun getConnectDirection(part: GateDiodePart): Direction = part.getInputSide()

        override fun getState(world: World, self: NetNode): Int = 0

        override fun setState(world: World, self: NetNode, state: Int) {
            getPart(world, self.pos)?.updateInputPower(state)
        }

        override fun getInput(world: World, self: NetNode): Int {
            val part = getPart(world, self.pos) ?: return 0
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
            return side.hashCode() xor -807492579
        }
    }

    data class Output(private val side: Direction) : GateDiodeBlockNode() {
        override val type = Type.OUTPUT

        override fun getSide(): Direction = side

        override fun getConnectDirection(part: GateDiodePart): Direction = part.getOutputSide()

        override fun getState(world: World, self: NetNode): Int {
            return getPart(world, self.pos)?.getTotalOutputPower() ?: 0
        }

        override fun setState(world: World, self: NetNode, state: Int) {
            getPart(world, self.pos)?.updateOutputReversePower(state)
        }

        override fun getInput(world: World, self: NetNode): Int {
            val part = getPart(world, self.pos) ?: return 0

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
            return side.hashCode() xor 1863451528
        }
    }

    object Decoder : BlockNodeDecoder {
        override fun createBlockNodeFromTag(tag: NbtElement?): BlockNode? {
            return BlockNodeUtil.readSidedTyped<Type>(tag) { side, type ->
                when (type) {
                    Type.INPUT -> Input(side)
                    Type.OUTPUT -> Output(side)
                }
            }
        }
    }

    protected enum class Type {
        INPUT, OUTPUT
    }
}
