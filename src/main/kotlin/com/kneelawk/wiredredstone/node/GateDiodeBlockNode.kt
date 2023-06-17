package com.kneelawk.wiredredstone.node

import com.kneelawk.graphlib.api.graph.NodeHolder
import com.kneelawk.graphlib.api.graph.user.BlockNodeDecoder
import com.kneelawk.graphlib.api.graph.user.BlockNodeType
import com.kneelawk.graphlib.api.wire.SidedWireConnectionFilter
import com.kneelawk.wiredredstone.logic.RedstoneCarrierFilter
import com.kneelawk.wiredredstone.logic.RedstoneWireType
import com.kneelawk.wiredredstone.part.AbstractGatePart
import com.kneelawk.wiredredstone.part.GateDiodePart
import com.kneelawk.wiredredstone.util.connectable.WireCornerBlockageFilter
import net.minecraft.nbt.NbtElement
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.minecraft.util.math.Direction
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

    override fun getType(): BlockNodeType = WRBlockNodes.GATE_DIODE

    override fun toTag(): NbtElement? = BlockNodeUtil.writeSidedType(side, type)

    data class Input(private val side: Direction) : GateDiodeBlockNode() {
        override val type = Type.INPUT

        override fun getSide(): Direction = side

        override fun getConnectDirection(part: GateDiodePart): Direction = part.getInputSide()

        override fun putPower(world: ServerWorld, self: NodeHolder<RedstoneCarrierBlockNode>, power: Int) {
            getPart(world, self.blockPos)?.updateInputPower(power)
        }

        override fun sourcePower(world: ServerWorld, self: NodeHolder<RedstoneCarrierBlockNode>): Int {
            val part = getPart(world, self.blockPos) ?: return 0
            val input = part.calculateInputPower()

            // Even though this gate's input does not output any signal to anything else in the network,
            // the gate's input itself is a network of one node, meaning that what's returned here gets
            // sent to setState anyways.
            return input
        }
    }

    data class Output(private val side: Direction) : GateDiodeBlockNode() {
        override val type = Type.OUTPUT

        override fun getSide(): Direction = side

        override fun getConnectDirection(part: GateDiodePart): Direction = part.getOutputSide()

        override fun putPower(world: ServerWorld, self: NodeHolder<RedstoneCarrierBlockNode>, power: Int) {
            getPart(world, self.blockPos)?.updateOutputReversePower(power)
        }

        override fun sourcePower(world: ServerWorld, self: NodeHolder<RedstoneCarrierBlockNode>): Int {
            val part = getPart(world, self.blockPos) ?: return 0

            // This is asking about input to the network, so we return either our output value or the value calculated
            // by redstone in the world.

            return max(part.outputPower, part.calculateOutputReversePower())
        }
    }

    object Decoder : BlockNodeDecoder {
        override fun decode(tag: NbtElement?): GateDiodeBlockNode? {
            return BlockNodeUtil.readSidedTyped<Type, _>(tag) { side, type, _ ->
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
