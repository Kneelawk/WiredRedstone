package com.kneelawk.wiredredstone.mixin.api;

import com.kneelawk.wiredredstone.mixin.impl.RenderLayerAccessor;
import com.kneelawk.wiredredstone.mixin.impl.RenderPhaseAccessor;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;

public class RenderLayerHelper {
    public static RenderLayer of(String name, VertexFormat vertexFormat, VertexFormat.DrawMode drawMode,
                                 int expectedBufferSize, boolean hasCrumbling, boolean translucent,
                                 RenderLayer.MultiPhaseParameters phases) {
        return RenderLayerAccessor
                .callOf(name, vertexFormat, drawMode, expectedBufferSize, hasCrumbling, translucent, phases);
    }

    public static RenderPhase.Transparency getNoTransparency() {
        return RenderPhaseAccessor.transpositioners$getNO_TRANSPARENCY();
    }

    public static RenderPhase.Transparency getTranslucentTransparency() {
        return RenderPhaseAccessor.transpositioners$getTRANSLUCENT_TRANSPARENCY();
    }

    public static RenderPhase.Shader getPositionTextureShader() {
        return RenderPhaseAccessor.transpositioners$getPOSITION_TEXTURE_SHADER();
    }

    public static RenderPhase.Texture getMipmapBlockAtlasTexture() {
        return RenderPhaseAccessor.transpositioners$getMIPMAP_BLOCK_ATLAS_TEXTURE();
    }
}
