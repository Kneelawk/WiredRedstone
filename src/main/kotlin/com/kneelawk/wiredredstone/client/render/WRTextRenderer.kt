package com.kneelawk.wiredredstone.client.render

import alexiil.mc.lib.multipart.api.AbstractPart
import alexiil.mc.lib.multipart.api.MultipartContainer
import alexiil.mc.lib.multipart.api.MultipartUtil
import com.kneelawk.wiredredstone.client.render.part.WRPartRenderers
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.WorldRenderer
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.Vec3d

/**
 * Gets part bakers to render text for each of their ports.
 */
@Environment(EnvType.CLIENT)
object WRTextRenderer {
    fun init() {
        WorldRenderEvents.AFTER_ENTITIES.register(::render)
    }

    private fun render(context: WorldRenderContext) {
        val world = context.world()
        val mc = MinecraftClient.getInstance()
        val player = mc.player
        val hit = mc.crosshairTarget
        val provider = context.consumers()!!

        if (player != null && player.isSneaking && hit is BlockHitResult) {
            val hitPos = hit.blockPos
            val key = MultipartUtil.get(world, hitPos)?.getSelectedPart(hit)?.modelKey

            if (key != null) {
                val light = WorldRenderer.getLightmapCoordinates(world, hitPos)
                val stack = context.matrixStack()
                val cameraPos = context.camera().pos

                stack.push()
                stack.translate(hitPos.x - cameraPos.x, hitPos.y - cameraPos.y, hitPos.z - cameraPos.z)

                WRPartRenderers.bakerFor(key::class)?.renderPortText(key, mc.textRenderer, stack, provider, light)

                stack.pop()
            }
        }
    }

    private fun MultipartContainer.getSelectedPart(hit: BlockHitResult): AbstractPart? {
        val vec = hit.pos.subtract(Vec3d.of(hit.blockPos))

        return getFirstPart {
            for (box in it.outlineShape.boundingBoxes) {
                if (box.expand(0.01).contains(vec)) {
                    return@getFirstPart true
                }
            }

            false
        }
    }
}
