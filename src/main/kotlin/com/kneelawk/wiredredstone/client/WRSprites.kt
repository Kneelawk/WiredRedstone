package com.kneelawk.wiredredstone.client

import com.kneelawk.wiredredstone.WRConstants
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback
import net.minecraft.client.texture.SpriteAtlasTexture

object WRSprites {
    fun init() {
        ClientSpriteRegistryCallback.event(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE)
            .register { _: SpriteAtlasTexture, registry: ClientSpriteRegistryCallback.Registry ->
                registry.register(WRConstants.id("block/red_alloy_wire_powered"))
            }
    }
}