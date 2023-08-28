package com.kneelawk.wiredredstone.mixin.api;

import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.render.RenderLayer;

import com.kneelawk.wiredredstone.mixin.impl.RenderLayerAccessor;

public class RenderLayerHelper {
    public static RenderLayer of(String name, VertexFormat vertexFormat, VertexFormat.DrawMode drawMode,
                                 int expectedBufferSize, boolean hasCrumbling, boolean translucent,
                                 RenderLayer.MultiPhaseParameters phases) {
        return RenderLayerAccessor
            .callOf(name, vertexFormat, drawMode, expectedBufferSize, hasCrumbling, translucent, phases);
    }
}
