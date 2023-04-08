package com.kneelawk.wiredredstone.node

import alexiil.mc.lib.multipart.api.MultipartUtil
import com.kneelawk.graphlib.api.v1.node.BlockNode
import com.kneelawk.graphlib.api.v1.node.BlockNodeDiscoverer
import com.kneelawk.wiredredstone.part.BlockNodeContainer
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos

object WRBlockNodeDiscoverer : BlockNodeDiscoverer {
    override fun getNodesInBlock(world: ServerWorld, pos: BlockPos): Collection<BlockNode> {
        val parts = MultipartUtil.get(world, pos)?.getParts(BlockNodeContainer::class.java).orEmpty()
        return parts.flatMap(BlockNodeContainer::createBlockNodes)
    }
}
