package com.kneelawk.wiredredstone.client.render

import com.kneelawk.wiredredstone.client.render.part.WRPartRenderers
import com.kneelawk.wiredredstone.item.GateItem
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.*
import net.minecraft.item.ItemUsageContext
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.HitResult

object WRGhostRenderer : VertexConsumerProvider {
    private val RENDER_LAYERS = Object2ObjectLinkedOpenHashMap<RenderLayer, BufferBuilder>()
    private val CONSUMERS: VertexConsumerProvider.Immediate =
        VertexConsumerProvider.immediate(RENDER_LAYERS, BufferBuilder(1 shl 12))

    init {
        RENDER_LAYERS[WRRenderLayers.GATE_PLACEMENT] = BufferBuilder(1 shl 12)
    }

    fun init() {
        WorldRenderEvents.END.register { context ->
            // Render in END because our translucent placement ghost would occlude chests otherwise.
            draw(context.camera())
        }
    }

    override fun getBuffer(renderLayer: RenderLayer): VertexConsumer {
        return CONSUMERS.getBuffer(renderLayer)
    }

    private fun draw(camera: Camera) {
        val matrices = WRMatrixFixer.getStack()

        val client = MinecraftClient.getInstance()
        client.player?.let { player ->
            // Render the placement ghost
            val target = client.crosshairTarget

            if (target is BlockHitResult && target.type == HitResult.Type.BLOCK) {
                for (hand in Hand.values()) {
                    val stack = player.getStackInHand(hand)
                    val item = stack.item
                    if (item is GateItem) {
                        val context = ItemUsageContext(player, hand, target)
                        val offer = item.getOfferForPlacementGhost(context)
                        if (offer != null) {
                            val mesh = offer.holder.part.modelKey?.let { key ->
                                WRPartRenderers.bakerFor(key::class)?.getMeshForPlacementGhost(key)
                            }

                            if (mesh != null) {
                                val pos = offer.holder.container.multipartPos
                                val cameraPos = camera.pos
                                val x = pos.x - cameraPos.x
                                val y = pos.y - cameraPos.y
                                val z = pos.z - cameraPos.z
                                matrices.push()
                                matrices.translate(x, y, z)
                                val consumer = getBuffer(WRRenderLayers.GATE_PLACEMENT)
                                RenderUtils.renderMesh(matrices, consumer, mesh)
                                matrices.pop()
                            }

                            break
                        }
                    }
                }
            }
        }

        WRMatrixFixer.renderSystemPush()
        CONSUMERS.draw()
        WRMatrixFixer.renderSystemPop()
    }
}
