package com.kneelawk.wiredredstone.mixin.api;

import com.kneelawk.wiredredstone.mixin.impl.FramebufferAccessor;
import net.minecraft.client.gl.Framebuffer;

public class FramebufferHelper {
    public static int getColorAttachment(Framebuffer fb) {
        return ((FramebufferAccessor) fb).getColorAttachment();
    }
}
