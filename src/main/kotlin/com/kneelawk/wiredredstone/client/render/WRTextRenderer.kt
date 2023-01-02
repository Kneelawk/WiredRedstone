package com.kneelawk.wiredredstone.client.render

import alexiil.mc.lib.multipart.api.MultipartUtil
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import com.kneelawk.wiredredstone.WRConstants
import com.kneelawk.wiredredstone.client.render.part.WRPartRenderers
import com.kneelawk.wiredredstone.util.getSelectedPart
import com.mojang.blaze3d.systems.RenderSystem
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gl.SimpleFramebuffer
import net.minecraft.client.render.*
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
    data class TextKey(val text: Text, val color: Int, val shadow: Boolean, val overline: Boolean, val background: Int)
    data class TextTexture(val texture: FramebufferTexture, val id: Identifier)

    private val immediate = VertexConsumerProvider.immediate(BufferBuilder(256))
    private val MC by lazy { MinecraftClient.getInstance() }
    private val TEXT_FB_CACHE: LoadingCache<TextKey, TextTexture> =
        CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.MINUTES)
            .removalListener<TextKey, TextTexture> { it.value?.texture?.close() }.build(CacheLoader.from(::makeTexture))
    private var TEXT_ID_MAP = mutableMapOf<TextKey, Identifier>()
    private var CUR_TEXT_ID = 1

    fun init() {
        WROverlayRenderer.RENDER_TO_OVERLAY.register(::render)
    }

    fun drawText(
        text: Text, color: Int, shadow: Boolean, overline: Boolean, model: Matrix4f, provider: VertexConsumerProvider,
        background: Int, light: Int
    ) {
        val width = MC.textRenderer.getWidth(text).toFloat()
        val height = MC.textRenderer.fontHeight.toFloat() + if (overline) 2 else 0

        val id = TEXT_FB_CACHE[TextKey(text, color, shadow, overline, background)].id

        val renderLayer = RenderLayer.getText(id)
        val consumer = provider.getBuffer(renderLayer)

        // the texture ends up up-side-down for some reason, so we flip it here
        consumer.vertex(model, 0f, height, 0f).color(-1).texture(0f, 0f).light(light).next()
        consumer.vertex(model, width, height, 0f).color(-1).texture(1f, 0f).light(light).next()
        consumer.vertex(model, width, 0f, 0f).color(-1).texture(1f, 1f).light(light).next()
        consumer.vertex(model, 0f, 0f, 0f).color(-1).texture(0f, 1f).light(light).next()
    }

    private fun render(context: WorldRenderContext) {
        val world = context.world()
        val player = MC.player
        val hit = MC.crosshairTarget
        val consumers = context.consumers()!!

        if (player != null && player.isSneaking && hit is BlockHitResult) {
            val hitPos = hit.blockPos
            val key = MultipartUtil.get(world, hitPos)?.getSelectedPart(hit.pos.subtract(Vec3d.of(hitPos)))?.modelKey

            if (key != null) {
                val stack = context.matrixStack()
                val cameraPos = context.camera().pos

                stack.push()
                stack.translate(hitPos.x - cameraPos.x, hitPos.y - cameraPos.y, hitPos.z - cameraPos.z)

                WRPartRenderers.bakerFor(key::class)?.renderOverlayText(key, stack, consumers)

                stack.pop()
            }
        }
    }

    private fun makeTexture(key: TextKey): TextTexture {
        // render everything to a framebuffer first because I like pain

        val text = key.text.asOrderedText()

        val yOffset = if (key.overline) 2 else 0
        val yOffsetF = yOffset.toFloat()
        val width = MC.textRenderer.getWidth(text)
        val height = MC.textRenderer.fontHeight + yOffset

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

        if (key.shadow) {
            val shadowColor = multiplyBrightness(key.color, 0.25f)

            if (key.overline) {
                drawRect(1f, 1f, width.toFloat() - 1f, 1f, shadowColor, textMat)
            }

            MC.textRenderer.draw(text, 1f, 1f + yOffsetF, shadowColor, false, textMat, immediate, false, 0, 15728880)
            immediate.draw()
            textMat.multiplyByTranslation(0f, 0f, -0.025f)
        }

        if (key.overline) {
            drawRect(0f, 0f, width.toFloat() - 1f, 1f, key.color, textMat)
        }

        MC.textRenderer.draw(text, 0f, yOffsetF, key.color, false, textMat, immediate, false, 0, 15728880)
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

        return (((alpha * 255.0f).toInt() and 0xFF) shl 24) or (((red * 255.0f).toInt() and 0xFF) shl 16) or (((green * 255.0f).toInt() and 0xFF) shl 8) or ((blue * 255.0f).toInt() and 0xFF)
    }

    private fun drawRect(x: Float, y: Float, width: Float, height: Float, color: Int, matrix4f: Matrix4f) {
        RenderSystem.setShader(GameRenderer::getPositionColorShader)
        val tessellator = Tessellator.getInstance()
        val buffer = tessellator.buffer

        val x2 = x + width
        val y2 = y + height

        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR)
        buffer.vertex(matrix4f, x2, y, 0f).color(color).next()
        buffer.vertex(matrix4f, x, y, 0f).color(color).next()
        buffer.vertex(matrix4f, x, y2, 0f).color(color).next()
        buffer.vertex(matrix4f, x2, y2, 0f).color(color).next()

        tessellator.draw()
    }
}
