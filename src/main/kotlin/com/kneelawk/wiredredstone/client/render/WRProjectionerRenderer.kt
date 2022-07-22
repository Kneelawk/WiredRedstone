package com.kneelawk.wiredredstone.client.render

import com.kneelawk.wiredredstone.item.ProjectionerItem
import com.kneelawk.wiredredstone.item.WRItems
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.minecraft.client.MinecraftClient

object WRProjectionerRenderer {
    fun init() {
        WorldRenderEvents.AFTER_ENTITIES.register(::render)
    }

    private fun render(context: WorldRenderContext) {
        val mc = MinecraftClient.getInstance()
        val player = mc.player ?: return

        val stack = if (player.mainHandStack.item == WRItems.PROJECTIONER) {
            player.mainHandStack
        } else if (player.offHandStack.item == WRItems.PROJECTIONER) {
            player.offHandStack
        } else return

        val ref = ProjectionerItem.getRef(stack) ?: return

        ref.renderProjection(context)
    }
}
