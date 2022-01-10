package com.kneelawk.wiredredstone.util

import com.kneelawk.wiredredstone.wirenet.Network
import com.kneelawk.wiredredstone.wirenet.RedstoneCarrierPartExt
import com.kneelawk.wiredredstone.wirenet.getWireNetworkState
import net.minecraft.block.Blocks
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.registry.RegistryKey
import net.minecraft.world.World
import java.util.*
import kotlin.math.max

object RedstoneLogic {
    var scheduled = mapOf<RegistryKey<World>, Set<UUID>>()
    var wiresGivePower = true

    fun scheduleUpdate(world: ServerWorld, pos: BlockPos) {
        // Could probably be optimised to only update the networks it needs to, but I can do that later.
        scheduled =
            scheduled + (world.registryKey to
                    (scheduled[world.registryKey].orEmpty()
                            + world.getWireNetworkState().controller.getNetworksAt(pos).map { it.id }))
    }

    fun flushUpdates(world: ServerWorld) {
        // I could theoretically do this in two passes, one for updating inputs, and one for updating outputs, but that
        // would not remove the 1-tick delay between wires connecting to inputs and outputs of gates, only the delay
        // between redstone dust connecting to inputs and wires connecting to outputs, causing inconsistent behavior
        // between wires and redstone dust when working with gates.

        // The only thing that would actually remove a delay between gate inputs and gate outputs would be the
        // graph-of-graphs solution. However that would likely be overkill for a redstone mod anyways. The
        // graph-of-graphs solution has its own drawbacks as well. Any kind of super-graph loop would always involve a
        // one-tick delay somewhere, but players would have no way of controlling where.

        val wireNetworkState = world.getWireNetworkState()
        for (id in scheduled[world.registryKey].orEmpty()) {
            val net = wireNetworkState.controller.getNetwork(id)
            if (net != null) updateState(world, net)
        }
        scheduled = scheduled - world.registryKey
    }

    fun updateState(world: World, network: Network) {
        val power = try {
            wiresGivePower = false
            network.getNodes()
                .constrainedMaxOfOrNull(0, 15) { (it.data.ext as RedstoneCarrierPartExt).getInput(world, it) }
        } finally {
            wiresGivePower = true
        }
        for (node in network.getNodes()) {
            val ext = node.data.ext as RedstoneCarrierPartExt
            ext.setState(world, node, power)
        }
    }

    fun getReceivingPower(
        world: World, pos: SidedPos, connections: UByte, receiveFromBottom: Boolean,
        blockage: UByte = BlockageUtils.UNBLOCKED
    ): Int {
        val offsetPos = pos.pos.offset(pos.side)
        val weakSides = Direction.values().filter { a ->
            val cardinal = RotationUtils.unrotatedDirection(pos.side, a)
            a.axis != pos.side.axis
                    && ConnectionUtils.isExternal(connections, cardinal)
                    && !BlockageUtils.isBlocked(blockage, cardinal)
        }
        return max(
            weakSides.constrainedMaxOfOrNull(0, 15) {
                val otherPos = pos.pos.offset(it)
                val otherState = world.getBlockState(otherPos)
                if (otherState.block == Blocks.REDSTONE_WIRE) {
                    0
                } else if (otherState.isSolidBlock(world, otherPos)) {
                    otherState.getStrongRedstonePower(world, otherPos, it)
                } else {
                    otherState.getWeakRedstonePower(world, otherPos, it)
                }
            },
            // FIXME: This only checks if the block immediately below is redstone wire. This should be checking if any
            //  of the blocks around that block are redstone wire. This is why redstone dust can power wire through
            //  blocks.
            if (receiveFromBottom && world.getBlockState(offsetPos).block != Blocks.REDSTONE_WIRE)
                world.getEmittedRedstonePower(offsetPos, pos.side) else 0
        )
    }
}