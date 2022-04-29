package com.kneelawk.wiredredstone.client.render

import com.kneelawk.wiredredstone.WRConstants.id
import com.kneelawk.wiredredstone.WRConstants.str
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.Shader
import net.minecraft.client.render.VertexFormats
import net.minecraft.resource.ResourceManager
import net.minecraft.resource.ResourceType
import net.minecraft.util.Identifier

object WRShaders {
    lateinit var GATE_PLACEMENT: Shader

    fun init() {
        WorldRenderEvents.START.register { ctx ->
            update(ctx.tickDelta())
        }
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES)
            .registerReloadListener(object : SimpleSynchronousResourceReloadListener {
                override fun reload(manager: ResourceManager) {
                    GATE_PLACEMENT = Shader(
                        manager, str("gate_placement"), VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL
                    )
                }

                override fun getFabricId(): Identifier = id("shader_reload")
            })
    }

    private fun update(tickDelta: Float) {
        var placementDelta = 0f

        val client = MinecraftClient.getInstance()
        client.player?.let { player ->
            val world = player.world
            placementDelta = world.time + tickDelta
        }

        // Setup shader uniforms
        GATE_PLACEMENT.getUniform("PlacementDelta")?.set(placementDelta)
    }
}
