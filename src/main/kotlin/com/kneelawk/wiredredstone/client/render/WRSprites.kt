package com.kneelawk.wiredredstone.client.render

import com.kneelawk.wiredredstone.client.render.part.WRPartRenderers
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback
import net.minecraft.client.texture.SpriteAtlasTexture

object WRSprites {
    fun init() {
        ClientSpriteRegistryCallback.event(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE)
            .register { _: SpriteAtlasTexture, registry: ClientSpriteRegistryCallback.Registry ->
                for (baker in WRPartRenderers.bakers()) {
                    baker.registerSprites(registry)
                }
            }
    }
}