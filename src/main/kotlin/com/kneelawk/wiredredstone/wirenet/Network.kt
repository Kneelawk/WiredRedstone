package com.kneelawk.wiredredstone.wirenet

import com.google.common.collect.HashMultimap
import com.kneelawk.wiredredstone.WRLog
import com.kneelawk.wiredredstone.util.SidedPos
import net.fabricmc.fabric.api.util.NbtType
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.util.math.BlockPos
import java.util.*

// This is almost completely copied from 2xsaiko's HCTM-Base.

class Network(val controller: WireNetworkController, val id: UUID) {
    companion object {
        fun fromTag(controller: WireNetworkController, tag: NbtCompound): Network? {
            val id = tag.getUuid("id")
            val network = Network(controller, id)
            val sNodes = tag.getList("nodes", NbtType.COMPOUND)
            val sLinks = tag.getList("links", NbtType.COMPOUND)

            val nodes = mutableListOf<NetNode?>()

            for (node in sNodes.map { it as NbtCompound }) {
                val part = NetworkPart.fromTag(node)
                nodes += if (part != null) {
                    network.createNode(part.pos, part.ext)
                } else {
                    // keep blank spaces for invalid nodes so links don't get messed up
                    null
                }
            }

            for (link in sLinks.map { it as NbtCompound }) {
                val first = nodes[link.getInt("first")]
                val second = nodes[link.getInt("second")]
                // val data = /* something */
                if (first != null && second != null) {
                    network.graph.link(first, second, null)
                }
            }

            network.rebuildRefs()

            return network
        }
    }

    private val graph = NetGraph()

    private val nodesInPos = HashMultimap.create<BlockPos, NetNode>()

    fun toTag(tag: NbtCompound): NbtCompound {
        val serializedNodes = mutableListOf<NbtCompound>()
        val serializedLinks = mutableListOf<NbtCompound>()
        val nodes = graph.nodes.toList()
        val n1 = nodes.withIndex().associate { it.value to it.index }
        for (node in nodes) {
            serializedNodes += node.data.toTag(NbtCompound())
        }
        for (link in nodes.flatMap { it.connections }.distinct()) {
            if (!n1.containsKey(link.first)) {
                WRLog.warn("Attempted to save link with non-existent node: ${link.first}")
                continue
            }
            if (!n1.containsKey(link.second)) {
                WRLog.warn("Attempted to save link with non-existent node: ${link.second}")
                continue
            }

            val sLink = NbtCompound()
            sLink.putInt("first", n1.getValue(link.first))
            sLink.putInt("second", n1.getValue(link.second))
            // sLink.put("data", link.data.toTag())
            serializedLinks += sLink
        }
        tag.put("nodes", NbtList().also { t -> serializedNodes.forEach { t.add(it) } })
        tag.put("links", NbtList().also { t -> serializedLinks.forEach { t.add(it) } })
        tag.putUuid("id", id)
        return tag
    }

    fun getNodesAt(pos: BlockPos) = nodesInPos[pos].toSet()

    fun getNodesAt(pos: SidedPos) =
        nodesInPos[pos.pos].filter { it.data.ext is SidedPartExt && it.data.ext.side == pos.side }.toSet()

    fun getNodes() = graph.nodes

    fun createNode(pos: BlockPos, ext: PartExt): NetNode {
        controller.changeListener()
        val node = graph.add(NetworkPart(pos, ext))
        nodesInPos.put(pos, node)
        controller.networksInPos.put(pos, this)
        controller.nodesToNetworks[node] = this.id
        controller.scheduleUpdate(node)
        return node
    }

    fun destroyNode(node: NetNode) {
        controller.changeListener()
        val connected = node.connections.map { it.other(node) }
        graph.remove(node)
        controller.scheduleUpdate(node)
        for (other in connected) controller.scheduleUpdate(other)

        split().forEach { controller.rebuildRefs(it.id) }

        if (graph.nodes.isEmpty()) controller.destroyNetwork(id)
        controller.rebuildRefs(id)
    }

    fun link(node1: NetNode, node2: NetNode) {
        graph.link(node1, node2, null)
        controller.scheduleUpdate(node1)
        controller.scheduleUpdate(node2)
    }

    fun unlink(node1: NetNode, node2: NetNode) {
        graph.unlink(node1, node2, null)
        controller.scheduleUpdate(node1)
        controller.scheduleUpdate(node2)
    }

    fun merge(other: Network) {
        controller.changeListener()
        if (other.id != id) {
            graph.join(other.graph)
            nodesInPos.putAll(other.nodesInPos)
            for (key in controller.networksInPos.keySet()) {
                controller.networksInPos.replaceValues(
                    key, controller.networksInPos.get(key).map { if (it == other) this else it }.toSet()
                )
            }
            controller.nodesToNetworks += graph.nodes.associate { it to this.id }
            controller.destroyNetwork(other.id)
        }
    }

    fun split(): Set<Network> {
        val newGraphs = graph.split()

        if (newGraphs.isNotEmpty()) {
            controller.changeListener()

            val networks = newGraphs.map {
                val net = controller.createNetwork()
                net.graph.join(it)
                net
            }

            networks.forEach { controller.rebuildRefs(it.id) }
            controller.rebuildRefs(id)

            return networks.toSet()
        }

        return emptySet()
    }

    fun rebuildRefs() {
        controller.changeListener()
        nodesInPos.clear()
        for (node in graph.nodes) {
            nodesInPos.put(node.data.pos, node)
        }
    }
}
