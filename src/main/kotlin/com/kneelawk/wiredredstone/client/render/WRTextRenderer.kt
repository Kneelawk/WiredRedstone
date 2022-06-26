package com.kneelawk.wiredredstone.client.render

import alexiil.mc.lib.multipart.api.AbstractPart
import alexiil.mc.lib.multipart.api.MultipartContainer
import alexiil.mc.lib.multipart.api.MultipartUtil
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import com.kneelawk.wiredredstone.WRConstants
import com.kneelawk.wiredredstone.client.render.part.WRPartRenderers
import com.mojang.blaze3d.systems.RenderSystem
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
import java.util.concurrent.TimeUnit

/**
 * Gets part bakers to render text for each of their ports.
 */
@Environment(EnvType.CLIENT)
object WRTextRenderer {
    data class TextKey(val text: Text, val color: Int, val shadow: Boolean, val background: Int)
    data class TextTexture(val texture: FramebufferTexture, val id: Identifier)

    private val MC = MinecraftClient.getInstance()
    private val TEXT_FB_CACHE: LoadingCache<TextKey, TextTexture> =
        CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.MINUTES)
            .removalListener<TextKey, TextTexture> { it.value?.texture?.close() }.build(CacheLoader.from(::makeTexture))
    private var TEXT_ID_MAP = mutableMapOf<TextKey, Identifier>()
    private var CUR_TEXT_ID = 1

    fun init() {
        WorldRenderEvents.AFTER_ENTITIES.register(::render)
    }

    fun drawText(
        text: Text, color: Int, shadow: Boolean, model: Matrix4f, provider: VertexConsumerProvider,
        seeThrough: Boolean, background: Int, light: Int
    ) {
        val width = MC.textRenderer.getWidth(text).toFloat()
        val height = MC.textRenderer.fontHeight.toFloat()

        val id = TEXT_FB_CACHE[TextKey(text, color, shadow, background)].id

        val renderLayer = if (seeThrough) RenderLayer.getTextSeeThrough(id) else RenderLayer.getText(id)
        val consumer = provider.getBuffer(renderLayer)

        // the texture ends up up-side-down for some reason, so we flip it here
        consumer.vertex(model, 0f, height, 0f).color(-1).texture(0f, 0f).light(light).next()
        consumer.vertex(model, width, height, 0f).color(-1).texture(1f, 0f).light(light).next()
        consumer.vertex(model, width, 0f, 0f).color(-1).texture(1f, 1f).light(light).next()
        consumer.vertex(model, 0f, 0f, 0f).color(-1).texture(0f, 1f).light(light).next()
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

    private fun makeTexture(key: TextKey): TextTexture {
        // render everything to a framebuffer first because I like pain

        val text = key.text.asOrderedText()

        val width = MC.textRenderer.getWidth(text)
        val height = MC.textRenderer.fontHeight
        val textMat = Matrix4f()
        textMat.loadIdentity()
        textMat.multiplyByTranslation(-1f, 1f, 0f)
        textMat.multiply(Matrix4f.scale(2f / width.toFloat(), -2f / height.toFloat(), 1f))
        textMat.multiplyByTranslation(0f, 0f, 0.05f)

        val fb = SimpleFramebuffer(width, height, true, MinecraftClient.IS_SYSTEM_MAC)

        val redBg = (key.background shr 16 and 0xFF).toFloat() / 255.0f
        val greenBg = (key.background shr 8 and 0xFF).toFloat() / 255.0f
        val blueBg = (key.background and 0xFF).toFloat() / 255.0f
        val alphaBg = (key.background shr 24 and 0xFF).toFloat() / 255.0f
        fb.setClearColor(redBg, greenBg, blueBg, alphaBg)

        fb.clear(MinecraftClient.IS_SYSTEM_MAC)
        fb.beginWrite(true)

        val modelViewStack = RenderSystem.getModelViewStack()
        modelViewStack.push()
        modelViewStack.loadIdentity()
        RenderSystem.applyModelViewMatrix()
        val backupProjMat = RenderSystem.getProjectionMatrix()
        RenderSystem.setProjectionMatrix(Matrix4f().apply { loadIdentity() })

        val immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().buffer)

        if (key.shadow) {
            val shadowColor = multiplyBrightness(key.color, 0.25f)
            MC.textRenderer.draw(text, 1f, 1f, shadowColor, false, textMat, immediate, false, 0, 15728880)
            textMat.multiplyByTranslation(0f, 0f, -0.025f)
        }

        MC.textRenderer.draw(text, 0f, 0f, key.color, false, textMat, immediate, false, 0, 15728880)
        immediate.draw()

        RenderSystem.setProjectionMatrix(backupProjMat)
        RenderSystem.getModelViewStack().pop()
        RenderSystem.applyModelViewMatrix()

        fb.endWrite()
        MinecraftClient.getInstance().framebuffer.beginWrite(true)

        val texture = FramebufferTexture(fb)

        val id = TEXT_ID_MAP.computeIfAbsent(key) {
            val number = CUR_TEXT_ID++
            WRConstants.id("text_fb/$number")
        }

        MC.textureManager.registerTexture(id, texture)

        return TextTexture(texture, id)
    }

    private fun multiplyBrightness(color: Int, brightness: Float): Int {
        val red = (color shr 16 and 0xFF).toFloat() / 255.0f * brightness
        val green = (color shr 8 and 0xFF).toFloat() / 255.0f * brightness
        val blue = (color and 0xFF).toFloat() / 255.0f * brightness
        val alpha = (color shr 24 and 0xFF).toFloat() / 255.0f

        return (((alpha * 255.0f).toInt() and 0xFF) shl 24) or
                (((red * 255.0f).toInt() and 0xFF) shl 16) or
                (((green * 255.0f).toInt() and 0xFF) shl 8) or
                ((blue * 255.0f).toInt() and 0xFF)
    }
}
