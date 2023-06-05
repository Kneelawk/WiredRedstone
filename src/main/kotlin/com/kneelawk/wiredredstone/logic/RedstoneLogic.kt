package com.kneelawk.wiredredstone.logic

import com.kneelawk.graphlib.api.graph.BlockGraph
import com.kneelawk.graphlib.api.util.SidedPos
import com.kneelawk.wiredredstone.node.WRBlockNodes
import com.kneelawk.wiredredstone.tag.WRBlockTags
import com.kneelawk.wiredredstone.util.RotationUtils
import com.kneelawk.wiredredstone.util.bits.BlockageUtils
import com.kneelawk.wiredredstone.util.bits.ConnectionUtils
import com.kneelawk.wiredredstone.util.constrainedMaxOf
import com.kneelawk.wiredredstone.util.threadLocal
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet
import it.unimi.dsi.fastutil.longs.LongSet
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.registry.RegistryKey
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World
import kotlin.math.max

object RedstoneLogic {
    private val scheduled = mutableMapOf<RegistryKey<World>, LongSet>()
    var wiresGivePower by threadLocal { true }

    fun init() {
        ServerTickEvents.END_WORLD_TICK.register(::flushUpdates)
    }

    fun scheduleUpdate(world: ServerWorld, pos: BlockPos) {
        // Could probably be optimised to only update the networks it needs to, but I can do that later.
        val set = scheduled.computeIfAbsent(world.registryKey) { LongLinkedOpenHashSet() }
        WRBlockNodes.WIRE_NET.getGraphWorld(world).getAllGraphIdsAt(pos).forEach(set::add)
    }

    fun scheduleUpdate(world: ServerWorld, graphId: Long) {
        val set = scheduled.computeIfAbsent(world.registryKey) { LongLinkedOpenHashSet() }
        set.add(graphId)
    }

    private fun flushUpdates(world: ServerWorld) {
        val controller = WRBlockNodes.WIRE_NET.getGraphWorld(world)

        // We're removing here because sometimes updating states needs to cause other graph updates to be scheduled,
        // but we can handle those next tick. I'm not ready to set use 0-tick recursive updates yet.
        val toUpdate = scheduled.remove(world.registryKey)
        if (toUpdate != null) {
            val updateIter = toUpdate.iterator()
            while (updateIter.hasNext()) {
                val id = updateIter.nextLong()
                val net = controller.getGraph(id)
                if (net != null) updateState(world, net)
            }
        }
    }

    fun updateState(world: ServerWorld, network: BlockGraph) {
        val power = try {
            wiresGivePower = false
            network.getCachedNodes(WRBlockNodes.REDSTONE_CARRIERS)
                .constrainedMaxOf(0, 15) { it.node.sourcePower(world, it) }
        } finally {
            wiresGivePower = true
        }
        for (node in network.getCachedNodes(WRBlockNodes.REDSTONE_CARRIERS)) {
            val ext = node.node
            ext.putPower(world, node, power)
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
