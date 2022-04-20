package com.kneelawk.wiredredstone.client.render

import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback
import net.minecraft.util.Identifier

data class WireIds(
    val topCross: Identifier, val topX: Identifier, val topZ: Identifier, val side: Identifier,
    val openEnd: Identifier, val closedEnd: Identifier? = null
) {
    fun register(registry: ClientSpriteRegistryCallback.Registry) {
        registry.register(topCross)
        registry.register(topX)
        registry.register(topZ)
        registry.register(side)
        registry.register(openEnd)
        closedEnd?.let(registry::register)
    }

    fun lookup(): WireSprites {
        return WireSprites(
            RenderUtils.getBlockSprite(topCross),
            RenderUtils.getBlockSprite(topX),
            RenderUtils.getBlockSprite(topZ),
            RenderUtils.getBlockSprite(side),
            RenderUtils.getBlockSprite(openEnd),
            closedEnd?.let(RenderUtils::getBlockSprite)
        )
    }
}
