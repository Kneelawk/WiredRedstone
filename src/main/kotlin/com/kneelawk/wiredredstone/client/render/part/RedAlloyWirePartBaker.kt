package com.kneelawk.wiredredstone.client.render.part

import alexiil.mc.lib.multipart.api.render.PartRenderContext
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import com.kneelawk.wiredredstone.client.render.*
import com.kneelawk.wiredredstone.part.key.RedAlloyWirePartKey
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh

object RedAlloyWirePartBaker : WRPartBaker<RedAlloyWirePartKey> {
    private val cache: LoadingCache<RedAlloyWirePartKey, Mesh> =
        CacheBuilder.newBuilder().build(CacheLoader.from(::makeMesh))

    override fun invalidateCaches() {
        cache.invalidateAll()
    }

    private fun makeMesh(key: RedAlloyWirePartKey): Mesh {
        val spriteId = if (key.powered) {
            WRSprites.RED_ALLOY_WIRE_POWERED_ID
        } else {
            WRSprites.RED_ALLOY_WIRE_UNPOWERED_ID
        }

        val sprite = RenderUtils.getBlockSprite(spriteId)

        val material = if (key.powered) {
            WRMaterials.POWERED_MATERIAL
        } else {
            WRMaterials.UNPOWERED_MATERIAL
        }

        val builder = RenderUtils.MESH_BUILDER
        val emitter = TransformingQuadEmitter.Single(builder.emitter, SideQuadTransform(key.side))

        WireRendering.emitWire(
            conn = key.connections,
            side = key.side,
            wireHeight = 2f / 16f,
            wireWidth = 2f / 16f,
            topCrossSprite = sprite,
            sideV = 7f / 16f,
            material = material,
            emitter = emitter
        )

        return builder.build()
    }

    override fun emitQuads(key: RedAlloyWirePartKey, ctx: PartRenderContext) {
        ctx.meshConsumer().accept(cache[key])
    }
}