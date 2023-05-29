package com.kneelawk.wiredredstone.node

import com.kneelawk.graphlib.api.graph.user.BlockNodeDecoder
import com.kneelawk.graphlib.api.wire.SidedWireConnectionFilter
import com.kneelawk.wiredredstone.logic.RedstoneCarrierFilter
import com.kneelawk.wiredredstone.logic.RedstoneWireType
import com.kneelawk.wiredredstone.part.AbstractGatePart
import com.kneelawk.wiredredstone.part.AbstractThreeInputGatePart
import com.kneelawk.wiredredstone.part.GateNorPart
import com.kneelawk.wiredredstone.util.NetNode
import com.kneelawk.wiredredstone.util.connectable.WireCornerBlockageFilter
import com.kneelawk.wiredredstone.util.toByte
import com.kneelawk.wiredredstone.util.toEnum
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

        override fun putPower(world: ServerWorld, self: NetNode, power: Int) {
            getPart(world, self.pos)?.updateInputPower(power, inputType)
        }

        override fun sourcePower(world: ServerWorld, self: NetNode): Int {
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

        override fun putPower(world: ServerWorld, self: NetNode, power: Int) {
            getPart(world, self.pos)?.updateOutputReversePower(power)
        }

        override fun sourcePower(world: ServerWorld, self: NetNode): Int {
            val part = getPart(world, self.pos) ?: return 0
            return max(part.outputPower, part.calculateOutputReversePower())
        }
    }

    object Decoder : BlockNodeDecoder {
        override fun decode(tag: NbtElement?): GateNorBlockNode? {
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
