package com.kneelawk.wiredredstone.node

import com.kneelawk.graphlib.api.graph.NodeHolder
import com.kneelawk.graphlib.api.graph.user.BlockNode
import com.kneelawk.graphlib.api.graph.user.BlockNodeType
import com.kneelawk.graphlib.api.util.HalfLink
import com.kneelawk.graphlib.api.wire.CenterWireBlockNode
import com.kneelawk.graphlib.api.wire.WireConnectionDiscoverers
import com.kneelawk.wiredredstone.logic.RedstoneCarrierFilter
import com.kneelawk.wiredredstone.logic.RedstoneLogic
import com.kneelawk.wiredredstone.logic.RedstoneWireType
import com.kneelawk.wiredredstone.part.StandingRedAlloyWirePart
import com.kneelawk.wiredredstone.util.RedstoneNode
import com.kneelawk.wiredredstone.util.getCenterPart
import net.minecraft.nbt.NbtElement
import net.minecraft.server.world.ServerWorld

object StandingRedAlloyBlockNode : CenterWireBlockNode, RedstoneCarrierBlockNode {
    private val filter = RedstoneCarrierFilter

    override val redstoneType: RedstoneWireType
        get() = RedstoneWireType.RedAlloy

    override fun getType(): BlockNodeType = WRBlockNodes.STANDING_RED_ALLOY_WIRE

    override fun toTag(): NbtElement? = null

    override fun findConnections(self: NodeHolder<BlockNode>): MutableCollection<HalfLink> {
        return WireConnectionDiscoverers.centerWireFindConnections(this, self, filter)
    }

    override fun canConnect(self: NodeHolder<BlockNode>, other: HalfLink): Boolean {
        return WireConnectionDiscoverers.centerWireCanConnect(this, self, other, filter)
    }

    override fun onConnectionsChanged(self: NodeHolder<BlockNode>) {
        val world = self.blockWorld
        if (world is ServerWorld) {
            RedstoneLogic.scheduleUpdate(world, self.graphId)
            self.getCenterPart<StandingRedAlloyWirePart>()?.updateInternalConnections(world)
        }
    }

    override fun putPower(world: ServerWorld, self: RedstoneNode, power: Int) {
        val part = self.getCenterPart<StandingRedAlloyWirePart>() ?: return
        part.updatePower(power)
        part.redraw()
    }

    override fun sourcePower(world: ServerWorld, self: RedstoneNode): Int {
        // TODO
        return 0
    }

    override fun isValid(self: NodeHolder<BlockNode>): Boolean {
        return self.getCenterPart<StandingRedAlloyWirePart>() != null
    }
}
