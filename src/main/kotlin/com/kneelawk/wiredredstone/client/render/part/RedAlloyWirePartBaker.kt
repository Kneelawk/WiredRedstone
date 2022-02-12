package com.kneelawk.wiredredstone.client.render.part

import alexiil.mc.lib.multipart.api.render.PartModelBaker
import alexiil.mc.lib.multipart.api.render.PartRenderContext
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import com.kneelawk.wiredredstone.client.render.*
import com.kneelawk.wiredredstone.part.key.RedAlloyWirePartKey
import io.vram.frex.fabric.compat.FabricMesh
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh

object RedAlloyWirePartBaker : PartModelBaker<RedAlloyWirePartKey> {
    private val cache: LoadingCache<RedAlloyWirePartKey, Mesh> =
        CacheBuilder.newBuilder().build(CacheLoader.from(::makeMesh))

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
        val emitter = builder.emitter.withTransformQuad(AbsentBlockInputContext, SideQuadTransform(key.side))

        RenderUtils.emitWire(key.connections, key.side.axis, 2f, 2f, sprite, sprite, 7f / 16f, material, emitter)

        return FabricMesh.of(builder.build())
    }

    override fun emitQuads(key: RedAlloyWirePartKey, ctx: PartRenderContext) {
        ctx.meshConsumer().accept(cache[key])
    }
}