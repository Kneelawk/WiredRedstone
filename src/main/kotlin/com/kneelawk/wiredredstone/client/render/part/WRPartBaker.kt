package com.kneelawk.wiredredstone.client.render.part

import alexiil.mc.lib.multipart.api.render.PartModelBaker
import alexiil.mc.lib.multipart.api.render.PartModelKey
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh
import net.minecraft.util.Identifier
import java.util.function.Consumer

interface WRPartBaker<K : PartModelKey> : PartModelBaker<K> {
    fun makeMesh(key: K): Mesh

    fun registerModels(out: Consumer<Identifier>) {}

    // Note: sprites only need be registered if they are not referenced from an existing model.
    fun registerSprites(registry: ClientSpriteRegistryCallback.Registry) {}

    fun invalidateCaches() {}
}