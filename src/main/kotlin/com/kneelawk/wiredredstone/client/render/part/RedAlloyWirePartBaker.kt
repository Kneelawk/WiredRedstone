package com.kneelawk.wiredredstone.client.render.part

import alexiil.mc.lib.multipart.api.render.PartRenderContext
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import com.kneelawk.wiredredstone.WRConstants
import com.kneelawk.wiredredstone.client.render.*
import com.kneelawk.wiredredstone.part.key.RedAlloyWirePartKey
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh

object RedAlloyWirePartBaker : WRPartBaker<RedAlloyWirePartKey> {
    private val RED_ALLOY_WIRE_POWERED_ID = WRConstants.id("block/red_alloy_wire_powered")
    private val RED_ALLOY_WIRE_UNPOWERED_ID = WRConstants.id("block/red_alloy_wire_unpowered")

    private val cache: LoadingCache<RedAlloyWirePartKey, Mesh> =
        CacheBuilder.newBuilder().build(CacheLoader.from(::makeMesh))

    private fun makeMesh(key: RedAlloyWirePartKey): Mesh {
        val spriteId = if (key.powered) {
            RED_ALLOY_WIRE_POWERED_ID
        } else {
            RED_ALLOY_WIRE_UNPOWERED_ID
        }

        val sprite = RenderUtils.getBlockSprite(spriteId)

        val material = if (key.powered) {
            WRMaterials.POWERED_MATERIAL
        } else {
            WRMaterials.UNPOWERED_MATERIAL
        }

        val builder = RenderUtils.MESH_BUILDER
        val emitter = TransformingQuadEmitter.Single(builder.emitter, SideQuadTransform(key.side))

        RenderUtils.emitWire(
            conn = key.connections,
            axis = key.side.axis,
            wireHeight = 2f,
            wireWidth = 2f,
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

    override fun registerSprites(registry: ClientSpriteRegistryCallback.Registry) {
        registry.register(RED_ALLOY_WIRE_POWERED_ID)
        registry.register(RED_ALLOY_WIRE_UNPOWERED_ID)
    }
}