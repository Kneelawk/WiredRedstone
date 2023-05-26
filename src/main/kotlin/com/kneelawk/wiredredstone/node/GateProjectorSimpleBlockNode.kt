package com.kneelawk.wiredredstone.node

import com.kneelawk.graphlib.api.node.BlockNodeDecoder
import com.kneelawk.graphlib.api.wire.SidedWireConnectionFilter
import com.kneelawk.wiredredstone.logic.RedstoneCarrierFilter
import com.kneelawk.wiredredstone.logic.RedstoneWireType
import com.kneelawk.wiredredstone.part.AbstractGatePart
import com.kneelawk.wiredredstone.part.GateProjectorSimplePart
import com.kneelawk.wiredredstone.util.NetNode
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

    override fun getTypeId(): Identifier = WRBlockNodes.GATE_PROJECTOR_SIMPLE_ID

    override fun getConnectDirection(part: GateProjectorSimplePart): Direction = part.getInputSide()

    override fun toTag(): NbtElement? {
        return NbtByte.of(side.id.toByte())
    }

    override fun putPower(world: ServerWorld, self: NetNode, power: Int) {
        getPart(world, self.pos)?.updateInputPower(power)
    }

    override fun sourcePower(world: ServerWorld, self: NetNode): Int {
        val part = getPart(world, self.pos) ?: return 0
        return part.calculateInputPower()
    }

    object Decoder : BlockNodeDecoder {
        override fun decode(tag: NbtElement?): GateProjectorSimpleBlockNode? {
            val side = Direction.byId((tag as? NbtByte ?: return null).intValue())
            return GateProjectorSimpleBlockNode(side)
        }
    }
}
