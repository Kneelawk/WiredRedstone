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
        scheduled =
            scheduled + (world.registryKey to (scheduled[world.registryKey].orEmpty() + world.getWireNetworkState().controller.getNetworksAt(
                pos
            ).map { it.id }))
    }

    fun flushUpdates(world: ServerWorld) {
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
            if (receiveFromBottom && world.getBlockState(offsetPos).block != Blocks.REDSTONE_WIRE)
                world.getEmittedRedstonePower(offsetPos, pos.side) else 0
        )
    }
}