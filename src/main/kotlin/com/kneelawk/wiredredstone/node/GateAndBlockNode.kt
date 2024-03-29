package com.kneelawk.wiredredstone.node

import com.kneelawk.graphlib.api.graph.NodeHolder
import com.kneelawk.graphlib.api.graph.user.BlockNodeDecoder
import com.kneelawk.graphlib.api.graph.user.BlockNodeType
import com.kneelawk.graphlib.api.wire.SidedWireConnectionFilter
import com.kneelawk.wiredredstone.logic.RedstoneCarrierFilter
import com.kneelawk.wiredredstone.logic.RedstoneWireType
import com.kneelawk.wiredredstone.part.AbstractGatePart
import com.kneelawk.wiredredstone.part.AbstractThreeInputGatePart
import com.kneelawk.wiredredstone.part.GateAndPart
import com.kneelawk.wiredredstone.util.connectable.WireCornerBlockageFilter
import com.kneelawk.wiredredstone.util.toByte
import com.kneelawk.wiredredstone.util.toEnum
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.minecraft.util.math.Direction
import kotlin.math.max

sealed class GateAndBlockNode : AbstractGateBlockNode<GateAndPart>(GateAndPart::class) {
    override val filter: SidedWireConnectionFilter by lazy {
        RedstoneCarrierFilter.and(
            WireCornerBlockageFilter(side, AbstractGatePart.CONNECTION_WIDTH, AbstractGatePart.CONNECTION_HEIGHT)
        )
    }

    override val redstoneType = RedstoneWireType.RedAlloy

    protected abstract val type: Type

    open fun writeExtra(tag: NbtCompound) {}

    override fun getType(): BlockNodeType = WRBlockNodes.GATE_AND

    override fun toTag(): NbtElement? = BlockNodeUtil.writeSidedType(side, type, ::writeExtra)

    data class Input(private val side: Direction, private val inputType: AbstractThreeInputGatePart.InputType) :
        GateAndBlockNode() {
        override val type = Type.INPUT

        override fun getSide(): Direction = side

        override fun getConnectDirection(part: GateAndPart): Direction = part.getInputSide(inputType)

        override fun putPower(world: ServerWorld, self: NodeHolder<RedstoneCarrierBlockNode>, power: Int) {
            getPart(world, self.blockPos)?.updateInputPower(power, inputType)
        }

        override fun sourcePower(world: ServerWorld, self: NodeHolder<RedstoneCarrierBlockNode>): Int {
            val part = getPart(world, self.blockPos) ?: return 0
            return part.calculateInputPower(inputType)
        }

        override fun writeExtra(tag: NbtCompound) {
            tag.putByte("inputType", inputType.toByte())
        }
    }

    data class Output(private val side: Direction) : GateAndBlockNode() {
        override val type = Type.OUTPUT

        override fun getSide(): Direction = side

        override fun getConnectDirection(part: GateAndPart): Direction = part.getOutputSide()

        override fun putPower(world: ServerWorld, self: NodeHolder<RedstoneCarrierBlockNode>, power: Int) {
            getPart(world, self.blockPos)?.updateOutputReversePower(power)
        }

        override fun sourcePower(world: ServerWorld, self: NodeHolder<RedstoneCarrierBlockNode>): Int {
            val part = getPart(world, self.blockPos) ?: return 0
            return max(part.outputPower, part.calculateOutputReversePower())
        }
    }

    object Decoder : BlockNodeDecoder {
        override fun decode(tag: NbtElement?): GateAndBlockNode? {
            return BlockNodeUtil.readSidedTyped<Type, _>(tag) { side, type, nbt ->
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
