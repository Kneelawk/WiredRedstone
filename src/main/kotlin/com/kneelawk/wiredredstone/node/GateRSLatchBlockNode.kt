package com.kneelawk.wiredredstone.node

import com.kneelawk.graphlib.api.v1.node.BlockNode
import com.kneelawk.graphlib.api.v1.node.BlockNodeDecoder
import com.kneelawk.graphlib.api.v1.wire.SidedWireConnectionFilter
import com.kneelawk.wiredredstone.logic.RedstoneCarrierFilter
import com.kneelawk.wiredredstone.logic.RedstoneWireType
import com.kneelawk.wiredredstone.part.AbstractGatePart
import com.kneelawk.wiredredstone.part.GateRSLatchPart
import com.kneelawk.wiredredstone.util.NetNode
import com.kneelawk.wiredredstone.util.connectable.WireCornerBlockageFilter
import com.kneelawk.wiredredstone.util.pos
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

    override fun getTypeId(): Identifier = WRBlockNodes.GATE_RS_LATCH

    override fun toTag(): NbtElement? = BlockNodeUtil.writeSidedType(side, type, ::writeExtra)

    data class Input(private val side: Direction, val latchState: GateRSLatchPart.LatchState) : GateRSLatchBlockNode() {
        override val type = Type.INPUT

        override fun getSide(): Direction = side

        override fun getConnectDirection(part: GateRSLatchPart): Direction = part.getInputSide(latchState)

        override fun putPower(world: ServerWorld, self: NetNode, power: Int) {
            getPart(world, self.pos)?.updateInputPower(power, latchState)
        }

        override fun sourcePower(world: ServerWorld, self: NetNode): Int {
            val part = getPart(world, self.pos) ?: return 0
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

        override fun putPower(world: ServerWorld, self: NetNode, power: Int) {
            getPart(world, self.pos)?.updateReverseOuputPower(power, latchState)
        }

        override fun sourcePower(world: ServerWorld, self: NetNode): Int {
            val part = getPart(world, self.pos) ?: return 0
            return max(part.getOutputPower(latchState), part.calculateOutputReversePower(latchState))
        }

        override fun writeExtra(tag: NbtCompound) {
            tag.putByte("latchState", latchState.toByte())
        }
    }

    object Decoder : BlockNodeDecoder {
        override fun createBlockNodeFromTag(tag: NbtElement?): BlockNode? {
            return BlockNodeUtil.readSidedTyped<Type>(tag) { side, type, tag ->
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
