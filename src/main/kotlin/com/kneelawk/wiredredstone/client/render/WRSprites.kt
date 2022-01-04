package com.kneelawk.wiredredstone.client.render

import com.kneelawk.wiredredstone.WRConstants
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback
import net.minecraft.client.texture.SpriteAtlasTexture

object WRSprites {
    val RED_ALLOY_WIRE_POWERED_ID = WRConstants.id("block/red_alloy_wire_powered")
    val RED_ALLOY_WIRE_UNPOWERED_ID = WRConstants.id("block/red_alloy_wire_unpowered")

    fun init() {
        ClientSpriteRegistryCallback.event(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE)
            .register { _: SpriteAtlasTexture, registry: ClientSpriteRegistryCallback.Registry ->
                registry.register(RED_ALLOY_WIRE_POWERED_ID)
                registry.register(RED_ALLOY_WIRE_UNPOWERED_ID)
            }
    }
}