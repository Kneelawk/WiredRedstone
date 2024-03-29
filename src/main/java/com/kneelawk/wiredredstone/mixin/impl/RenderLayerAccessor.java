package com.kneelawk.wiredredstone.mixin.impl;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.render.RenderLayer;

@Mixin(RenderLayer.class)
public interface RenderLayerAccessor {
    @Invoker
    static RenderLayer.MultiPhase callOf(String name, VertexFormat vertexFormat, VertexFormat.DrawMode drawMode,
                                         int expectedBufferSize, boolean hasCrumbling, boolean translucent,
                                         RenderLayer.MultiPhaseParameters phases) {
        throw new IllegalStateException("RenderLayerAccessor mixin error");
    }
}
