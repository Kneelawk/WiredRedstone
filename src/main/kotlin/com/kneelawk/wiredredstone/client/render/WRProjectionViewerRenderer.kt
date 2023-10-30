package com.kneelawk.wiredredstone.client.render

import com.kneelawk.wiredredstone.item.ProjectionViewerUtil
import com.kneelawk.wiredredstone.item.WRItems
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.minecraft.client.MinecraftClient

object WRProjectionViewerRenderer {
    fun init() {
        WROverlayRenderer.RENDER_TO_OVERLAY.register(::render)
    }

    private fun render(context: WorldRenderContext) {
        val mc = MinecraftClient.getInstance()
        val player = mc.player ?: return

        val stack = if (player.mainHandStack.item == WRItems.PROJECTION_VIEWER) {
            player.mainHandStack
        } else if (player.offHandStack.item == WRItems.PROJECTION_VIEWER) {
            player.offHandStack
        } else return

        val ref = ProjectionViewerUtil.getRef(stack) ?: return

        ref.renderProjection(context)
    }
}
