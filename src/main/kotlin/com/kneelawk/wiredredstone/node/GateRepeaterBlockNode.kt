package com.kneelawk.wiredredstone.node

import com.kneelawk.graphlib.api.graph.NodeHolder
import com.kneelawk.graphlib.api.graph.user.BlockNodeDecoder
import com.kneelawk.graphlib.api.wire.SidedWireConnectionFilter
import com.kneelawk.wiredredstone.logic.RedstoneCarrierFilter
import com.kneelawk.wiredredstone.logic.RedstoneWireType
import com.kneelawk.wiredredstone.part.AbstractGatePart
import com.kneelawk.wiredredstone.part.GateRepeaterPart
import com.kneelawk.wiredredstone.util.connectable.WireCornerBlockageFilter
import net.minecraft.nbt.NbtElement
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.minecraft.util.math.Direction
import kotlin.math.max

sealed class GateRepeaterBlockNode : AbstractGateBlockNode<GateRepeaterPart>(GateRepeaterPart::class) {
    override val filter: SidedWireConnectionFilter by lazy {
        RedstoneCarrierFilter.and(
            WireCornerBlockageFilter(side, AbstractGatePart.CONNECTION_WIDTH, AbstractGatePart.CONNECTION_HEIGHT)
        )
    }

    override val redstoneType = RedstoneWireType.RedAlloy

    protected abstract val type: Type

    override fun getTypeId(): Identifier = WRBlockNodes.GATE_REPEATER_ID

    override fun toTag(): NbtElement? = BlockNodeUtil.writeSidedType(side, type)

    data class Input(private val side: Direction) : GateRepeaterBlockNode() {
        override val type = Type.INPUT

        override fun getSide(): Direction = side

        override fun getConnectDirection(part: GateRepeaterPart): Direction = part.getInputSide()

        override fun putPower(world: ServerWorld, self: NodeHolder<RedstoneCarrierBlockNode>, power: Int) {
            getPart(world, self.pos)?.updateInputPower(power)
        }

        override fun sourcePower(world: ServerWorld, self: NodeHolder<RedstoneCarrierBlockNode>): Int {
            val part = getPart(world, self.pos) ?: return 0
            return part.calculateInputPower()
        }
    }

    data class Output(private val side: Direction) : GateRepeaterBlockNode() {
        override val type = Type.OUTPUT

        override fun getSide(): Direction = side

        override fun getConnectDirection(part: GateRepeaterPart): Direction = part.getOutputSide()

        override fun putPower(world: ServerWorld, self: NodeHolder<RedstoneCarrierBlockNode>, power: Int) {
            getPart(world, self.pos)?.updateOutputReversePower(power)
        }

        override fun sourcePower(world: ServerWorld, self: NodeHolder<RedstoneCarrierBlockNode>): Int {
            val part = getPart(world, self.pos) ?: return 0
            return max(part.outputPower, part.calculateOutputReversePower())
        }
    }

    object Decoder : BlockNodeDecoder {
        override fun decode(tag: NbtElement?): GateRepeaterBlockNode? {
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
