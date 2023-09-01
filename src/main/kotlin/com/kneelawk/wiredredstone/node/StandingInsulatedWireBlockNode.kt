package com.kneelawk.wiredredstone.node

import alexiil.mc.lib.multipart.api.AbstractPart
import com.kneelawk.graphlib.api.graph.NodeHolder
import com.kneelawk.graphlib.api.graph.user.BlockNode
import com.kneelawk.graphlib.api.graph.user.BlockNodeDecoder
import com.kneelawk.graphlib.api.graph.user.BlockNodeType
import com.kneelawk.graphlib.api.wire.CenterWireBlockNode
import com.kneelawk.wiredredstone.logic.RedstoneLogic
import com.kneelawk.wiredredstone.logic.RedstoneWireType
import com.kneelawk.wiredredstone.part.StandingInsulatedWirePart
import com.kneelawk.wiredredstone.util.RedstoneNode
import com.kneelawk.wiredredstone.util.getCenterPart
import net.minecraft.nbt.NbtByte
import net.minecraft.nbt.NbtElement
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.DyeColor

data class StandingInsulatedWireBlockNode(val color: DyeColor) : CenterWireBlockNode, RedstoneCarrierBlockNode,
    PartBlockNode {
    override val redstoneType = RedstoneWireType.Colored(color)

    override fun getType(): BlockNodeType = WRBlockNodes.STANDING_INSULATED_WIRE

    override fun toTag(): NbtElement {
        return NbtByte.of(color.id.toByte())
    }

    override fun onConnectionsChanged(self: NodeHolder<BlockNode>) {
        val world = self.blockWorld
        if (world is ServerWorld) {
            RedstoneLogic.scheduleUpdate(world, self.graphId)
            self.getCenterPart<StandingInsulatedWirePart>()?.updateInternalConnections(world)
        }
    }

    override fun putPower(world: ServerWorld, self: RedstoneNode, power: Int) {
        val part = self.getCenterPart<StandingInsulatedWirePart>() ?: return
        part.updatePower(power)
        part.redraw()
    }

    override fun sourcePower(world: ServerWorld, self: RedstoneNode): Int {
        val part = self.getCenterPart<StandingInsulatedWirePart>() ?: return 0
        return part.getReceivingPower()
    }

    override fun isValid(self: NodeHolder<BlockNode>): Boolean {
        return self.getCenterPart<StandingInsulatedWirePart>() != null
    }

    override fun getPart(self: NodeHolder<BlockNode>): AbstractPart? {
        return self.getCenterPart<StandingInsulatedWirePart>()
    }

    object Decoder : BlockNodeDecoder {
        override fun decode(tag: NbtElement?): BlockNode? {
            val byte = tag as? NbtByte ?: return null
            return StandingInsulatedWireBlockNode(DyeColor.byId(byte.intValue()))
        }
    }
}
