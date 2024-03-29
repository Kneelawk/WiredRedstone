package com.kneelawk.wiredredstone.node

import com.kneelawk.graphlib.api.graph.NodeHolder
import com.kneelawk.graphlib.api.graph.user.BlockNodeDecoder
import com.kneelawk.graphlib.api.graph.user.BlockNodeType
import com.kneelawk.graphlib.api.wire.SidedWireConnectionFilter
import com.kneelawk.wiredredstone.logic.RedstoneCarrierFilter
import com.kneelawk.wiredredstone.logic.RedstoneWireType
import com.kneelawk.wiredredstone.part.AbstractGatePart
import com.kneelawk.wiredredstone.part.GateProjectorSimplePart
import com.kneelawk.wiredredstone.util.connectable.WireCornerBlockageFilter
import net.minecraft.nbt.NbtByte
import net.minecraft.nbt.NbtElement
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.minecraft.util.math.Direction

data class GateProjectorSimpleBlockNode(private val side: Direction) :
    AbstractGateBlockNode<GateProjectorSimplePart>(GateProjectorSimplePart::class) {
    override val filter: SidedWireConnectionFilter = RedstoneCarrierFilter.and(
        WireCornerBlockageFilter(side, AbstractGatePart.CONNECTION_WIDTH, AbstractGatePart.CONNECTION_HEIGHT)
    )

    override val redstoneType = RedstoneWireType.RedAlloy

    override fun getSide(): Direction = side

    override fun getType(): BlockNodeType = WRBlockNodes.GATE_PROJECTOR_SIMPLE

    override fun getConnectDirection(part: GateProjectorSimplePart): Direction = part.getInputSide()

    override fun toTag(): NbtElement? {
        return NbtByte.of(side.id.toByte())
    }

    override fun putPower(world: ServerWorld, self: NodeHolder<RedstoneCarrierBlockNode>, power: Int) {
        getPart(world, self.blockPos)?.updateInputPower(power)
    }

    override fun sourcePower(world: ServerWorld, self: NodeHolder<RedstoneCarrierBlockNode>): Int {
        val part = getPart(world, self.blockPos) ?: return 0
        return part.calculateInputPower()
    }

    object Decoder : BlockNodeDecoder {
        override fun decode(tag: NbtElement?): GateProjectorSimpleBlockNode? {
            val side = Direction.byId((tag as? NbtByte ?: return null).intValue())
            return GateProjectorSimpleBlockNode(side)
        }
    }
}
