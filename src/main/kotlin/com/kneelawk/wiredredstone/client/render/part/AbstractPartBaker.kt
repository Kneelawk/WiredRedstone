package com.kneelawk.wiredredstone.client.render.part

import alexiil.mc.lib.multipart.api.render.PartModelKey
import alexiil.mc.lib.multipart.api.render.PartRenderContext
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh

abstract class AbstractPartBaker<K : PartModelKey> : WRPartBaker<K> {
    private val cache: LoadingCache<K, Mesh> =
        CacheBuilder.newBuilder().build(CacheLoader.from(::makeMesh))

    protected abstract fun makeMesh(key: K): Mesh

    override fun invalidateCaches() {
        cache.invalidateAll()
    }

    override fun getMeshForPlacementGhost(key: K): Mesh {
        return cache[key]
    }

    override fun emitQuads(key: K, ctx: PartRenderContext) {
        ctx.meshConsumer().accept(cache[key])
    }
}
