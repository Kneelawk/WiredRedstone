package com.kneelawk.wiredredstone.node

import com.kneelawk.graphlib.graph.BlockNode
import com.kneelawk.graphlib.graph.BlockNodeDecoder
import com.kneelawk.graphlib.wire.SidedWireConnectionFilter
import com.kneelawk.wiredredstone.logic.RedstoneCarrierFilter
import com.kneelawk.wiredredstone.logic.RedstoneWireType
import com.kneelawk.wiredredstone.part.AbstractGatePart
import com.kneelawk.wiredredstone.part.GateNotPart
import com.kneelawk.wiredredstone.util.NetNode
import com.kneelawk.wiredredstone.util.connectable.WireCornerBlockageFilter
import com.kneelawk.wiredredstone.util.pos
import net.minecraft.nbt.NbtElement
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.minecraft.util.math.Direction
import kotlin.math.max

sealed class GateNotBlockNode : AbstractGateBlockNode<GateNotPart>(GateNotPart::class) {
    override val filter: SidedWireConnectionFilter by lazy {
        RedstoneCarrierFilter.and(
            WireCornerBlockageFilter(side, AbstractGatePart.CONNECTION_WIDTH, AbstractGatePart.CONNECTION_HEIGHT)
        )
    }

    override val redstoneType = RedstoneWireType.RedAlloy

    protected abstract val type: Type

    override fun getTypeId(): Identifier = WRBlockNodes.GATE_NOT_ID

    override fun toTag(): NbtElement? = BlockNodeUtil.writeSidedType(side, type)

    data class Input(private val side: Direction) : GateNotBlockNode() {
        override val type = Type.INPUT

        override fun getSide(): Direction = side

        override fun getConnectDirection(part: GateNotPart): Direction = part.getInputSide()

        override fun putPower(world: ServerWorld, self: NetNode, power: Int) {
            getPart(world, self.pos)?.updateInputPower(power)
        }

        override fun sourcePower(world: ServerWorld, self: NetNode): Int {
            val part = getPart(world, self.pos) ?: return 0
            return part.calculateInputPower()
        }
    }

    data class Output(private val side: Direction) : GateNotBlockNode() {
        override val type = Type.OUTPUT

        override fun getSide(): Direction = side

        override fun getConnectDirection(part: GateNotPart): Direction = part.getOutputSide()

        override fun putPower(world: ServerWorld, self: NetNode, power: Int) {
            getPart(world, self.pos)?.updateOutputReversePower(power)
        }

        override fun sourcePower(world: ServerWorld, self: NetNode): Int {
            val part = getPart(world, self.pos) ?: return 0
            return max(part.outputPower, part.calculateOutputReversePower())
        }
    }

    object Decoder : BlockNodeDecoder {
        override fun createBlockNodeFromTag(tag: NbtElement?): BlockNode? {
            return BlockNodeUtil.readSidedTyped<Type>(tag) { side, type, _ ->
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
