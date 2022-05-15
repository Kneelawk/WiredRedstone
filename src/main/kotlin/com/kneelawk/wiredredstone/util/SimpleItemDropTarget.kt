package com.kneelawk.wiredredstone.util

import alexiil.mc.lib.multipart.api.AbstractPart
import net.minecraft.entity.ItemEntity
import net.minecraft.item.ItemStack
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.Vec3d
import net.minecraft.world.GameRules

class SimpleItemDropTarget(val world: ServerWorld, val origin: Vec3d) : AbstractPart.ItemDropTarget {
    override fun drop(stack: ItemStack) {
        drop(stack, origin)
    }

    override fun drop(stack: ItemStack, pos: Vec3d) {
        val dx = world.random.nextDouble() * 0.2 - 0.1
        val dy = 0.2
        val dz = world.random.nextDouble() * 0.2 - 0.1
        drop(stack, pos, Vec3d(dx, dy, dz))
    }

    override fun drop(stack: ItemStack, pos: Vec3d, velocity: Vec3d) {
        if (world.isClient || stack.isEmpty || !world.gameRules.getBoolean(GameRules.DO_TILE_DROPS)) {
            return
        }
        val itemEntity = ItemEntity(world, pos.x, pos.y, pos.z, stack, velocity.x, velocity.y, velocity.z)
        itemEntity.setToDefaultPickupDelay()
        world.spawnEntity(itemEntity)
    }

    override fun dropsAsEntity(): Boolean {
        return true
    }
}