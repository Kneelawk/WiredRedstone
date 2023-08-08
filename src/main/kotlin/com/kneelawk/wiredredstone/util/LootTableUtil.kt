package com.kneelawk.wiredredstone.util

import alexiil.mc.lib.multipart.api.AbstractPart
import net.minecraft.loot.context.LootContextParameterSet
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec3d

object LootTableUtil {
    fun addPartDrops(
        part: AbstractPart, target: AbstractPart.ItemDropTarget, params: LootContextParameterSet, partId: Identifier
    ) {
        val world = part.getWorld()
        if (world is ServerWorld) {
            val lootTableId = Identifier(partId.namespace, "parts/${partId.path}")
            val lootTable = world.server.lootManager.getLootTable(lootTableId)
            val dropPos = Vec3d.ofCenter(part.container.multipartPos)
            lootTable.generateLoot(params) { target.drop(it, dropPos, Vec3d.ZERO) }
        }
    }
}
