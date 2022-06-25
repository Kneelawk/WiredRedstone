package com.kneelawk.wiredredstone.client.render

import alexiil.mc.lib.multipart.api.AbstractPart
import alexiil.mc.lib.multipart.api.MultipartContainer
import alexiil.mc.lib.multipart.api.MultipartUtil
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import com.kneelawk.wiredredstone.WRConstants
import com.kneelawk.wiredredstone.client.render.part.WRPartRenderers
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gl.SimpleFramebuffer
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.WorldRenderer
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.Matrix4f
import net.minecraft.util.math.Vec3d

/**
 * Gets part bakers to render text for each of their ports.
 */
@Environment(EnvType.CLIENT)
object WRTextRenderer {
    data class TextKey(val text: Text, val color: Int, val shadow: Boolean, val background: Int)

    private val TEXTURE_MANAGER by lazy { MinecraftClient.getInstance().textureManager }
    private val TEXT_RENDERER by lazy { MinecraftClient.getInstance().textRenderer }
    private val FRAMEBUFFER_CACHE: LoadingCache<TextKey, Identifier> =
        CacheBuilder.newBuilder().build(CacheLoader.from(::makeTexture))
    private val TEXT_ID_MAP = mutableMapOf<TextKey, Int>()
    private var CUR_TEXT_ID = 1

    private fun makeTexture(key: TextKey): Identifier {
        val addedSize = if (key.shadow) 1 else 0

        val text = key.text.asOrderedText()

        val width = TEXT_RENDERER.getWidth(text) + addedSize
        val height = TEXT_RENDERER.fontHeight + addedSize
        val textMat = Matrix4f()

        val fb = SimpleFramebuffer(width, height, false, MinecraftClient.IS_SYSTEM_MAC)

        fb.beginWrite(true)
        fb.clear(MinecraftClient.IS_SYSTEM_MAC)
        val immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().buffer)
        TEXT_RENDERER.draw(text, 0f, 0f, key.color, key.shadow, textMat, immediate, false, key.background, 15728880)
        immediate.draw()
        fb.endWrite()
        MinecraftClient.getInstance().framebuffer.beginWrite(true)

        val texture = FramebufferTexture(fb)
        val id = textureId(key)
        TEXTURE_MANAGER.registerTexture(id, texture)

        return id
    }

    private fun textureId(key: TextKey): Identifier {
        val id = TEXT_ID_MAP.computeIfAbsent(key) { CUR_TEXT_ID++ }
        return WRConstants.id("text_fb/$id")
    }

    fun init() {
        WorldRenderEvents.AFTER_ENTITIES.register(::render)
    }

    fun drawText(
        text: Text, color: Int, shadow: Boolean, model: Matrix4f, provider: VertexConsumerProvider,
        seeThrough: Boolean, background: Int, light: Int
    ) {
        val addedSize = if (shadow) 1 else 0

        val width = (TEXT_RENDERER.getWidth(text) + addedSize).toFloat()
        val height = (TEXT_RENDERER.fontHeight + addedSize).toFloat()

        val id = FRAMEBUFFER_CACHE[TextKey(text, color, shadow, background)]

        val renderLayer = if (seeThrough) RenderLayer.getTextSeeThrough(id) else RenderLayer.getText(id)
        val consumer = provider.getBuffer(renderLayer)

        consumer.vertex(model, 0f, height, 0f).color(-1).texture(0f, 1f).light(light).next()
        consumer.vertex(model, width, height, 0f).color(-1).texture(1f, 1f).light(light).next()
        consumer.vertex(model, width, 0f, 0f).color(-1).texture(1f, 0f).light(light).next()
        consumer.vertex(model, 0f, 0f, 0f).color(-1).texture(0f, 0f).light(light).next()
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

                WRPartRenderers.bakerFor(key::class)?.renderOverlayText(key, stack, provider, light)

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
