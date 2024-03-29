package com.kneelawk.wiredredstone.node

import com.kneelawk.graphlib.api.graph.NodeHolder
import com.kneelawk.graphlib.api.graph.user.BlockNodeDecoder
import com.kneelawk.graphlib.api.graph.user.BlockNodeType
import com.kneelawk.graphlib.api.wire.SidedWireConnectionFilter
import com.kneelawk.wiredredstone.logic.RedstoneCarrierFilter
import com.kneelawk.wiredredstone.logic.RedstoneWireType
import com.kneelawk.wiredredstone.part.AbstractGatePart
import com.kneelawk.wiredredstone.part.GateRSLatchPart
import com.kneelawk.wiredredstone.util.connectable.WireCornerBlockageFilter
import com.kneelawk.wiredredstone.util.toByte
import com.kneelawk.wiredredstone.util.toEnum
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.minecraft.util.math.Direction
import kotlin.math.max

sealed class GateRSLatchBlockNode : AbstractGateBlockNode<GateRSLatchPart>(GateRSLatchPart::class) {
    override val filter: SidedWireConnectionFilter by lazy {
        RedstoneCarrierFilter.and(
            WireCornerBlockageFilter(side, AbstractGatePart.CONNECTION_WIDTH, AbstractGatePart.CONNECTION_HEIGHT)
        )
    }

    override val redstoneType = RedstoneWireType.RedAlloy

    protected abstract val type: Type

    protected abstract fun writeExtra(tag: NbtCompound)

    override fun getType(): BlockNodeType = WRBlockNodes.GATE_RS_LATCH

    override fun toTag(): NbtElement? = BlockNodeUtil.writeSidedType(side, type, ::writeExtra)

    data class Input(private val side: Direction, val latchState: GateRSLatchPart.LatchState) : GateRSLatchBlockNode() {
        override val type = Type.INPUT

        override fun getSide(): Direction = side

        override fun getConnectDirection(part: GateRSLatchPart): Direction = part.getInputSide(latchState)

        override fun putPower(world: ServerWorld, self: NodeHolder<RedstoneCarrierBlockNode>, power: Int) {
            getPart(world, self.blockPos)?.updateInputPower(power, latchState)
        }

        override fun sourcePower(world: ServerWorld, self: NodeHolder<RedstoneCarrierBlockNode>): Int {
            val part = getPart(world, self.blockPos) ?: return 0
            return part.calculateInputPower(latchState)
        }

        override fun writeExtra(tag: NbtCompound) {
            tag.putByte("latchState", latchState.toByte())
        }
    }

    data class Output(private val side: Direction, val latchState: GateRSLatchPart.LatchState) :
        GateRSLatchBlockNode() {
        override val type = Type.OUTPUT

        override fun getSide(): Direction = side

        override fun getConnectDirection(part: GateRSLatchPart): Direction = part.getOutputSide(latchState)

        override fun putPower(world: ServerWorld, self: NodeHolder<RedstoneCarrierBlockNode>, power: Int) {
            getPart(world, self.blockPos)?.updateReverseOuputPower(power, latchState)
        }

        override fun sourcePower(world: ServerWorld, self: NodeHolder<RedstoneCarrierBlockNode>): Int {
            val part = getPart(world, self.blockPos) ?: return 0
            return max(part.getOutputPower(latchState), part.calculateOutputReversePower(latchState))
        }

        override fun writeExtra(tag: NbtCompound) {
            tag.putByte("latchState", latchState.toByte())
        }
    }

    object Decoder : BlockNodeDecoder {
        override fun decode(nbt: NbtElement?): GateRSLatchBlockNode? {
            return BlockNodeUtil.readSidedTyped<Type, _>(nbt) { side, type, tag ->
                when (type) {
                    Type.INPUT -> Input(side, tag.getByte("latchState").toEnum())
                    Type.OUTPUT -> Output(side, tag.getByte("latchState").toEnum())
                }
            }
        }
    }

    enum class Type {
        INPUT, OUTPUT
    }
}
