package com.kneelawk.wiredredstone.client.render

import com.kneelawk.wiredredstone.WRConstants
import com.kneelawk.wiredredstone.mixin.api.RenderLayerHelper
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.RenderPhase
import net.minecraft.client.render.VertexFormat
import net.minecraft.client.render.VertexFormats

class WRRenderLayers private constructor(name: String, beginAction: Runnable, endAction: Runnable) :
    RenderPhase(name, beginAction, endAction) {
    companion object {
        val GATE_PLACEMENT: RenderLayer = RenderLayerHelper.of(
            WRConstants.str("gate_placement"),
            VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL,
            VertexFormat.DrawMode.QUADS,
            256,
            false,
            true,
            RenderLayer.MultiPhaseParameters.builder()
                .program(ENTITY_TRANSLUCENT_PROGRAM)
                .texture(MIPMAP_BLOCK_ATLAS_TEXTURE)
                .transparency(TRANSLUCENT_TRANSPARENCY)
                .lightmap(ENABLE_LIGHTMAP)
                .overlay(ENABLE_OVERLAY_COLOR)
                .build(false)
        )
    }
}
