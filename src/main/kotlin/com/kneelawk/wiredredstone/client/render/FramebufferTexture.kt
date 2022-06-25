package com.kneelawk.wiredredstone.client.render

import com.kneelawk.wiredredstone.mixin.api.FramebufferHelper
import net.minecraft.client.gl.Framebuffer
import net.minecraft.client.texture.AbstractTexture
import net.minecraft.resource.ResourceManager

class FramebufferTexture(val fb: Framebuffer) : AbstractTexture() {
    override fun load(manager: ResourceManager) {
        // NO-OP
    }

    override fun getGlId(): Int {
        return FramebufferHelper.getColorAttachment(fb)
    }

    override fun clearGlId() {
        // NO-OP
    }

    override fun close() {
        fb.delete()
    }
}
