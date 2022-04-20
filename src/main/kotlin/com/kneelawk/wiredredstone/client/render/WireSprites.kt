package com.kneelawk.wiredredstone.client.render

import net.minecraft.client.texture.Sprite

data class WireSprites(
    val topCross: Sprite, val topX: Sprite, val topZ: Sprite, val side: Sprite, val openEnd: Sprite,
    val closedEnd: Sprite? = null
)
