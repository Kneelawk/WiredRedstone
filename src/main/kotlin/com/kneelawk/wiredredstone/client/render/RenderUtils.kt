package com.kneelawk.wiredredstone.client.render

import net.minecraft.client.MinecraftClient
import net.minecraft.client.texture.Sprite
import net.minecraft.client.texture.SpriteAtlasTexture
import net.minecraft.util.Identifier

object RenderUtils {
    fun getBlockSprite(id: Identifier): Sprite {
        return MinecraftClient.getInstance().getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE).apply(id)
    }
}