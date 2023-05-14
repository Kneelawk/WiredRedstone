package com.kneelawk.wiredredstone.client.render

import com.kneelawk.wiredredstone.client.render.WROverlayRenderer.RenderToOverlay
import com.mojang.blaze3d.systems.RenderSystem
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gl.SimpleFramebuffer
import net.minecraft.client.render.BufferBuilder
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider

private val layerBuffers = Object2ObjectLinkedOpenHashMap<RenderLayer, BufferBuilder>().apply {
    put(RenderLayer.getTranslucent(), BufferBuilder(256))
}
private val immediate = VertexConsumerProvider.immediate(layerBuffers, BufferBuilder(256))

object WROverlayRenderer : VertexConsumerProvider by immediate {
    private val MC by lazy { MinecraftClient.getInstance() }
    private val framebuffer by lazy {
        val window = MC.window
        SimpleFramebuffer(
            window.framebufferWidth, window.framebufferHeight, true, MinecraftClient.IS_SYSTEM_MAC
        ).apply {
            setClearColor(0f, 0f, 0f, 0f)
        }
    }

    val RENDER_TO_OVERLAY: Event<RenderToOverlay> =
        EventFactory.createArrayBacked(RenderToOverlay::class.java, RenderToOverlay {}) { listeners ->
            RenderToOverlay {
                for (listener in listeners) {
                    listener.renderToOverlay(it)
                }
            }
        }

    fun init() {
        WorldRenderEvents.END.register(::render)
    }

    private fun render(context: WorldRenderContext) {
        val newContext = object : WorldRenderContext by context {
            override fun consumers(): VertexConsumerProvider = immediate
        }

        val window = MC.window

        if (window.framebufferWidth != framebuffer.textureWidth || window.framebufferHeight != framebuffer.textureHeight) {
            framebuffer.resize(window.framebufferWidth, window.framebufferHeight, MinecraftClient.IS_SYSTEM_MAC)
        }

        framebuffer.clear(MinecraftClient.IS_SYSTEM_MAC)

        framebuffer.beginWrite(false)

        RENDER_TO_OVERLAY.invoker().renderToOverlay(newContext)

        immediate.draw()

        MC.framebuffer.beginWrite(false)

        // Framebuffer.draw() messes with the projection matrix, so we're keeping a backup.
        val projBackup = RenderSystem.getProjectionMatrix()
        RenderSystem.enableBlend()
        framebuffer.draw(window.framebufferWidth, window.framebufferHeight, false)
        RenderSystem.disableBlend()
        RenderSystem.setProjectionMatrix(projBackup, RenderSystem.getVertexSorting())
    }

    fun interface RenderToOverlay {
        fun renderToOverlay(context: WorldRenderContext)
    }
}
