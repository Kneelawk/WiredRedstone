package com.kneelawk.wiredredstone.client.render

import com.kneelawk.wiredredstone.WRConstants
import com.kneelawk.wiredredstone.mixin.api.RenderLayerHelper
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.RenderPhase
import net.minecraft.client.render.VertexFormat
import net.minecraft.client.render.VertexFormats

object WRRenderLayers {
    private val GATE_PLACEMENT_SHADER = RenderPhase.Shader(WRShaders::GATE_PLACEMENT)

    val GATE_PLACEMENT: RenderLayer = RenderLayerHelper.of(
        WRConstants.str("gate_placement"),
        VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL,
        VertexFormat.DrawMode.QUADS,
        1 shl 12,
        false,
        true,
        RenderLayer.MultiPhaseParameters.builder().shader(GATE_PLACEMENT_SHADER)
            .texture(RenderLayerHelper.getMipmapBlockAtlasTexture())
            .transparency(RenderLayerHelper.getTranslucentTransparency()).build(false)
    )
}