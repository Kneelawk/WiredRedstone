package com.kneelawk.wiredredstone.client.render

import com.mojang.blaze3d.framebuffer.Framebuffer
import net.minecraft.client.texture.AbstractTexture
import net.minecraft.client.texture.MissingSprite
import net.minecraft.resource.ResourceManager

class FramebufferTexture(val fb: Framebuffer) : AbstractTexture() {
    override fun load(manager: ResourceManager) {
        // NO-OP
    }

    override fun getGlId(): Int {
        val id = fb.colorAttachment

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
