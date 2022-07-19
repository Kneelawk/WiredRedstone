package com.kneelawk.wiredredstone.node

import com.kneelawk.graphlib.graph.BlockNode
import com.kneelawk.graphlib.graph.BlockNodeDecoder
import com.kneelawk.graphlib.wire.SidedWireConnectionFilter
import com.kneelawk.wiredredstone.part.AbstractGatePart
import com.kneelawk.wiredredstone.part.AbstractThreeInputGatePart
import com.kneelawk.wiredredstone.part.GateNorPart
import com.kneelawk.wiredredstone.util.*
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.minecraft.util.math.Direction
import kotlin.math.max

sealed class GateNorBlockNode : AbstractGateBlockNode<GateNorPart>(GateNorPart::class) {
    override val filter: SidedWireConnectionFilter by lazy {
        RedstoneCarrierFilter.and(
            WireCornerBlockageFilter(side, AbstractGatePart.CONNECTION_WIDTH, AbstractGatePart.CONNECTION_HEIGHT)
        )
    }

    override val redstoneType = RedstoneWireType.RedAlloy

    protected abstract val type: Type

    open fun writeExtra(tag: NbtCompound) {}

    override fun getTypeId(): Identifier = WRBlockNodes.GATE_NOR_ID

    override fun toTag(): NbtElement? = BlockNodeUtil.writeSidedType(side, type, ::writeExtra)

    data class Input(private val side: Direction, private val inputType: AbstractThreeInputGatePart.InputType) :
        GateNorBlockNode() {
        override val type = Type.INPUT

        override fun getSide(): Direction = side

        override fun getConnectDirection(part: GateNorPart): Direction = part.getInputSide(inputType)

        override fun getState(world: ServerWorld, self: NetNode): Int = 0

        override fun setState(world: ServerWorld, self: NetNode, state: Int) {
            getPart(world, self.pos)?.updateInputPower(state, inputType)
        }

        override fun getInput(world: ServerWorld, self: NetNode): Int {
            val part = getPart(world, self.pos) ?: return 0
            return part.calculateInputPower(inputType)
        }

        override fun writeExtra(tag: NbtCompound) {
            tag.putByte("inputType", inputType.toByte())
        }
    }

    data class Output(private val side: Direction) : GateNorBlockNode() {
        override val type = Type.OUTPUT

        override fun getSide(): Direction = side

        override fun getConnectDirection(part: GateNorPart): Direction = part.getOutputSide()

        override fun getState(world: ServerWorld, self: NetNode): Int {
            return getPart(world, self.pos)?.getTotalOutputPower() ?: 0
        }

        override fun setState(world: ServerWorld, self: NetNode, state: Int) {
            getPart(world, self.pos)?.updateOutputReversePower(state)
        }

        override fun getInput(world: ServerWorld, self: NetNode): Int {
            val part = getPart(world, self.pos) ?: return 0
            return max(part.outputPower, part.calculateOutputReversePower())
        }
    }

    object Decoder : BlockNodeDecoder {
        override fun createBlockNodeFromTag(tag: NbtElement?): BlockNode? {
            return BlockNodeUtil.readSidedTyped<Type>(tag) { side, type, nbt ->
                when (type) {
                    Type.INPUT -> Input(side, nbt.getByte("inputType").toEnum())
                    Type.OUTPUT -> Output(side)
                }
            }
        }
    }

    protected enum class Type {
        INPUT, OUTPUT
    }
}
