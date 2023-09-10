package com.kneelawk.wiredredstone.client.render

import alexiil.mc.lib.multipart.api.MultipartContainer
import com.kneelawk.wiredredstone.client.render.part.WRPartRenderers
import com.kneelawk.wiredredstone.item.GateItem
import com.kneelawk.wiredredstone.mixin.api.MatrixHelper
import com.mojang.blaze3d.vertex.BufferBuilder
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.LightmapTextureManager
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.item.ItemUsageContext
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.HitResult

object WRGhostRenderer {
    private val immediate = VertexConsumerProvider.immediate(BufferBuilder(256))

    fun init() {
        WorldRenderEvents.END.register(::draw)
    }

    private fun draw(context: WorldRenderContext) {
        val client = MinecraftClient.getInstance()
        client.player?.let { player ->
            // Render the placement ghost
            val target = client.crosshairTarget

            if (target is BlockHitResult && target.type == HitResult.Type.BLOCK) {
                for (hand in Hand.values()) {
                    val stack = player.getStackInHand(hand)
                    val item = stack.item
                    if (item is GateItem) {
                        val hitContext = ItemUsageContext(player, hand, target)
                        val offer = item.getOfferForPlacementGhost(hitContext)
                        if (offer != null) {
                            val mesh = offer.holder.part.modelKey?.let { key ->
                                WRPartRenderers.bakerFor(key::class)?.getMeshForPlacementGhost(key)
                            }

                            if (mesh != null) {
                                MatrixHelper.setupProjectionMatrix(context)
                                renderPlacementGhost(context, offer, mesh)
                            }

                            break
                        }
                    }
                }
            }
        }
    }

    private fun renderPlacementGhost(
        context: WorldRenderContext,
        offer: MultipartContainer.PartOffer,
        mesh: Mesh
    ) {
        val camera = context.camera()
        val matrices = context.matrixStack()

        val pos = offer.holder.container.multipartPos
        val cameraPos = camera.pos
        val x = pos.x - cameraPos.x
        val y = pos.y - cameraPos.y
        val z = pos.z - cameraPos.z
        matrices.push()
        matrices.translate(x, y, z)

        val consumer = GhostVertexConsumer(immediate.getBuffer(WRRenderLayers.GATE_PLACEMENT))
        RenderUtils.renderMesh(matrices, consumer, mesh, LightmapTextureManager.MAX_LIGHT_COORDINATE)

        matrices.pop()

        immediate.draw()
    }
}
