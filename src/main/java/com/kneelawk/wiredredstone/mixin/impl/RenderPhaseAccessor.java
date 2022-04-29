package com.kneelawk.wiredredstone.mixin.impl;

import net.minecraft.client.render.RenderPhase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderPhase.class)
public interface RenderPhaseAccessor {
    @Accessor("NO_TRANSPARENCY")
    static RenderPhase.Transparency transpositioners$getNO_TRANSPARENCY() {
        throw new IllegalStateException("RenderPhaseAccessor mixin error");
    }

    @Accessor("TRANSLUCENT_TRANSPARENCY")
    static RenderPhase.Transparency transpositioners$getTRANSLUCENT_TRANSPARENCY() {
        throw new IllegalStateException("RenderPhaseAccessor mixin error");
    }

    @Accessor("POSITION_TEXTURE_SHADER")
    static RenderPhase.Shader transpositioners$getPOSITION_TEXTURE_SHADER() {
        throw new IllegalStateException("RenderPhaseAccessor mixin error");
    }

    @Accessor("MIPMAP_BLOCK_ATLAS_TEXTURE")
    static RenderPhase.Texture transpositioners$getMIPMAP_BLOCK_ATLAS_TEXTURE() {
        throw new IllegalStateException("RenderPhaseAccessor mixin error");
    }
}
