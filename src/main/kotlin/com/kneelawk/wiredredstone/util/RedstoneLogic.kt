package com.kneelawk.wiredredstone.util

import com.kneelawk.graphlib.GraphLib
import com.kneelawk.graphlib.graph.BlockGraph
import com.kneelawk.graphlib.util.SidedPos
import com.kneelawk.wiredredstone.node.RedstoneCarrierBlockNode
import com.kneelawk.wiredredstone.tag.WRBlockTags
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet
import it.unimi.dsi.fastutil.longs.LongSet
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.registry.RegistryKey
import net.minecraft.world.World
import kotlin.math.max

object RedstoneLogic {
    val scheduled = mutableMapOf<RegistryKey<World>, LongSet>()
    var wiresGivePower = true

    fun scheduleUpdate(world: ServerWorld, pos: BlockPos) {
        // Could probably be optimised to only update the networks it needs to, but I can do that later.
        val set = scheduled.computeIfAbsent(world.registryKey) { LongLinkedOpenHashSet() }
        GraphLib.getController(world).getGraphsAt(pos).forEach(set::add)
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

        val controller = GraphLib.getController(world)
        for (id in scheduled[world.registryKey].orEmpty()) {
            val net = controller.getGraph(id)
            if (net != null) updateState(world, net)
        }
        scheduled -= world.registryKey
    }

    fun updateState(world: ServerWorld, network: BlockGraph) {
        val power = try {
            wiresGivePower = false
            network.nodes
                .constrainedMaxOf(0, 15) { (it.node as RedstoneCarrierBlockNode).getInput(world, it) }
        } finally {
            wiresGivePower = true
        }
        for (node in network.nodes) {
            val ext = node.node as RedstoneCarrierBlockNode
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
            weakSides.constrainedMaxOf(0, 15) {
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
            if (receiveFromBottom && Direction.values()
                    .none { world.getBlockState(offsetPos.offset(it)).block == Blocks.REDSTONE_WIRE }
            ) {
                world.getEmittedRedstonePower(offsetPos, pos.side)
            } else {
                val state = world.getBlockState(offsetPos)
                max(
                    state.getWeakRedstonePower(world, offsetPos, pos.side),
                    state.getStrongRedstonePower(world, offsetPos, pos.side)
                )
            }
        )
    }

    fun shouldWireConnect(state: BlockState): Boolean {
        return (state.emitsRedstonePower() || state.isIn(WRBlockTags.WIRE_FORCE_CONNECTABLE))
                && !state.isIn(WRBlockTags.WIRE_FORCE_NOT_CONNECTABLE)
    }
}
