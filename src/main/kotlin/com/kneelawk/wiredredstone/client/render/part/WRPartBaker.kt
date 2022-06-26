package com.kneelawk.wiredredstone.client.render.part

import alexiil.mc.lib.multipart.api.render.PartModelBaker
import alexiil.mc.lib.multipart.api.render.PartModelKey
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier
import java.util.function.Consumer

interface WRPartBaker<K : PartModelKey> : PartModelBaker<K> {
    fun getMeshForPlacementGhost(key: K): Mesh? = null

    fun renderOverlayText(key: K, stack: MatrixStack, provider: VertexConsumerProvider, light: Int) {}

    fun registerModels(out: Consumer<Identifier>) {}

    // Note: sprites only need be registered if they are not referenced from an existing model.
    fun registerSprites(registry: ClientSpriteRegistryCallback.Registry) {}

    fun invalidateCaches() {}
}
