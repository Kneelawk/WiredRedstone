package com.kneelawk.wiredredstone.wirenet

import alexiil.mc.lib.multipart.api.MultipartUtil
import com.kneelawk.graphlib.GraphLib
import com.kneelawk.graphlib.graph.BlockNode
import com.kneelawk.graphlib.graph.BlockNodeDiscoverer
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos

object WRBlockNodeDiscoverer : BlockNodeDiscoverer {
    override fun getNodesInBlock(world: ServerWorld, pos: BlockPos): Collection<BlockNode> {
        val parts = MultipartUtil.get(world, pos)?.getParts(NetNodeContainer::class.java).orEmpty()
        return parts.flatMap(NetNodeContainer::createExtsForContainer)
    }

    fun init() {
        GraphLib.registerDiscoverer(this)
    }
}