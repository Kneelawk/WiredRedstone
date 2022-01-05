package com.kneelawk.wiredredstone.util

import alexiil.mc.lib.multipart.api.AbstractPart
import net.minecraft.loot.context.LootContext
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.minecraft.world.World

object LootTableUtil {
    fun addPartDrops(world: World, target: AbstractPart.ItemDropTarget, context: LootContext, partId: Identifier) {
        if (world is ServerWorld) {
            val lootTableId = Identifier(partId.namespace, "parts/${partId.path}")
            val lootTable = world.server.lootManager.getTable(lootTableId)
            lootTable.generateLoot(context, target::drop)
        }
    }
}