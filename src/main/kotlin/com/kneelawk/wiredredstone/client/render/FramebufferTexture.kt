package com.kneelawk.wiredredstone.client.render

import com.kneelawk.wiredredstone.mixin.api.FramebufferHelper
import net.minecraft.client.gl.Framebuffer
import net.minecraft.client.texture.AbstractTexture
import net.minecraft.client.texture.MissingSprite
import net.minecraft.resource.ResourceManager

class FramebufferTexture(val fb: Framebuffer) : AbstractTexture() {
    override fun load(manager: ResourceManager) {
        // NO-OP
    }

    override fun getGlId(): Int {
        val id = FramebufferHelper.getColorAttachment(fb)

        if (id < 0) {
            return MissingSprite.getMissingSpriteTexture().glId
        }

        return id
    }

    override fun clearGlId() {
        // NO-OP
    }

    override fun close() {
        fb.delete()
    }
}
