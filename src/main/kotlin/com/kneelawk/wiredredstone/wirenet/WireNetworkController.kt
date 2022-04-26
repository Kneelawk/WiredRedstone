package com.kneelawk.wiredredstone.wirenet

import alexiil.mc.lib.multipart.api.MultipartUtil
import com.google.common.collect.LinkedHashMultimap
import com.kneelawk.wiredredstone.util.SidedPos
import net.fabricmc.fabric.api.util.NbtType
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import java.util.*

// This is almost completely copied from 2xsaiko's HCTM-Base.

class WireNetworkController(val world: ServerWorld, val changeListener: () -> Unit) {
    companion object {
        fun fromTag(tag: NbtCompound, world: ServerWorld, changeListener: () -> Unit): WireNetworkController {
            val controller = WireNetworkController(world, changeListener)

            val sNetworks = tag.getList("networks", NbtType.COMPOUND)
            for (sNetwork in sNetworks.map { it as NbtCompound }) {
                val net = Network.fromTag(controller, sNetwork) ?: continue
                controller.networks += net.id to net
            }

            controller.rebuildRefs()
            controller.cleanup()
            return controller
        }
    }

    private val networks = mutableMapOf<UUID, Network>()
    val networksInPos = LinkedHashMultimap.create<BlockPos, Network>()
    val nodesToNetworks = mutableMapOf<NetNode, UUID>()

    private var changed = setOf<NetNode>()

    fun toTag(): NbtCompound {
        val tag = NbtCompound()
        val list = NbtList()
        networks.values.map { it.toTag(NbtCompound()) }.forEach { list.add(it) }
        tag.put("networks", list)
        return tag
    }

    fun getNodesAt(pos: BlockPos): Sequence<NetNode> {
        return networksInPos[pos].asSequence().flatMap { net -> net.getNodesAt(pos) }.distinct()
    }

    fun getNodesAt(pos: SidedPos): Sequence<NetNode> {
        return networksInPos[pos.pos].asSequence().flatMap { net -> net.getNodesAt(pos) }.distinct()
    }

    fun getNetworksAt(pos: BlockPos): Set<Network> {
        return networksInPos[pos]
    }

    fun rebuildRefs(vararg networks: UUID) {
        changeListener()
        val toRebuild = networks.takeIf { it.isNotEmpty() }?.map { Pair(it, this.networks[it]) }
            ?: this.networks.entries.map { Pair(it.key, it.value) }

        val toRemove = mutableListOf<Pair<BlockPos, Network>>()

        for ((id, net) in toRebuild) {
            for ((pos, netInPos) in networksInPos.entries()) {
                if (netInPos.id == id) toRemove.add(Pair(pos, netInPos))
            }
            for ((pos, netInPos) in toRemove) {
                networksInPos.remove(pos, netInPos)
            }
            toRemove.clear()

            nodesToNetworks -= nodesToNetworks.filterValues { it == id }.keys

            if (net != null) {
                net.rebuildRefs()
                net.getNodes().onEach { nodesToNetworks[it] = net.id }.map { it.data.pos }
                    .forEach { networksInPos.put(it, net) }
            }
        }
    }

    fun cleanup() {
        for (net in networks.values.toSet()) {
            if (net.getNodes().isEmpty()) {
                destroyNetwork(net.id)
            }
        }
    }

    fun createNetwork(): Network {
        changeListener()
        val net = Network(this, UUID.randomUUID())
        networks += net.id to net
        return net
    }

    fun destroyNetwork(id: UUID) {
        changeListener()
        networks -= id

        for ((k, v) in networksInPos.entries().toSet()) {
            if (v.id == id) networksInPos.remove(k, v)
        }

        nodesToNetworks -= nodesToNetworks.filter { it.value == id }.keys
    }

    fun scheduleUpdate(node: NetNode) {
        changed = changed + node
    }

    fun flushUpdates() {
        while (changed.isNotEmpty()) {
            val n = changed.first()
            n.data.ext.onChanged(n, world, n.data.pos)
            changed = changed - n
        }
    }

    /**
     * Called by `AbstractSidedPart` when a sided part is added or removed.
     */
    fun onChanged(world: ServerWorld, pos: BlockPos) {
        val parts = MultipartUtil.get(world, pos)?.getParts(NetNodeContainer::class.java).orEmpty()
        val worldExts = parts.flatMap { it.partExtType.createExtsForContainer(world, pos, it) }.toSet()

        onNodesChanged(pos, world, worldExts)
    }

    private fun onNodesChanged(pos: BlockPos, world: ServerWorld, worldExts: Set<PartExt>) {
        val new = worldExts.toMutableSet()

        for (net in networksInPos[pos].toSet()) {
            for (node in net.getNodesAt(pos).toList()) {
                if (node.data.ext !in worldExts) {
                    net.destroyNode(node)
                }
                new -= node.data.ext
            }
        }

        for (ext in new) {
            val net = createNetwork()
            val node = net.createNode(pos, ext)
            updateNodeConnections(world, node)
        }
    }

    fun updateConnections(world: ServerWorld, pos: SidedPos) {
        getNodesAt(pos).toList().forEach { updateNodeConnections(world, it) }
    }

    private fun updateNodeConnections(world: ServerWorld, node: NetNode) {
        changeListener()
        val nodeNetId = getNetIdForNode(node)

        val nv = NodeView(world)
        val oldConnections = node.connections.map { it.other(node) }.toSet()
        val ids = node.data.ext.tryConnect(node, world, node.data.pos, nv)
        val returnedConnections = ids.filter { node in it.data.ext.tryConnect(it, world, it.data.pos, nv) }.toSet()
        val newConnections = returnedConnections.filter { getNetIdForNode(it) != nodeNetId || it !in oldConnections }
        val removedConnections = oldConnections.filter { it !in returnedConnections }

        for (other in newConnections) {
            val net = networks.getValue(nodeNetId)
            if (getNetIdForNode(other) != nodeNetId) {
                val otherNet = networks.getValue(getNetIdForNode(other))
                net.merge(otherNet)
            }

            net.link(node, other)
        }

        val net = networks.getValue(nodeNetId)
        for (other in removedConnections) {
            net.unlink(node, other)
        }

        if (removedConnections.isNotEmpty()) {
            // This is expensive, so I want to avoid it if at all possible
            net.split().forEach { rebuildRefs(it.id) }
            if (net.getNodes().isEmpty()) destroyNetwork(net.id)
            rebuildRefs(net.id)
        }
    }

    fun getNetIdForNode(node: NetNode) = nodesToNetworks.getValue(node)

    fun getNetwork(id: UUID): Network? {
        return networks[id]
    }
}
