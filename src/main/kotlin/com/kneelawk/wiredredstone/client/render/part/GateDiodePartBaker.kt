package com.kneelawk.wiredredstone.client.render.part

import alexiil.mc.lib.multipart.api.render.PartModelBaker
import alexiil.mc.lib.multipart.api.render.PartRenderContext
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import com.kneelawk.wiredredstone.client.render.*
import com.kneelawk.wiredredstone.part.key.GateDiodePartKey
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh

object GateDiodePartBaker : PartModelBaker<GateDiodePartKey> {
    private val cache: LoadingCache<GateDiodePartKey, Mesh> =
        CacheBuilder.newBuilder().build(CacheLoader.from(::makeMesh))

    private fun makeMesh(key: GateDiodePartKey): Mesh {
        val modelId = if (key.powered) {
            WRModels.GATE_DIODE_ON
        } else {
            WRModels.GATE_DIODE_OFF
        }

        val backgroundModel = RenderUtils.getModel(WRModels.GATE_DIODE_BACKGROUND)
        val redstoneModel = RenderUtils.getModel(modelId)

        val material = if (key.powered) {
            WRMaterials.POWERED_MATERIAL
        } else {
            WRMaterials.UNPOWERED_MATERIAL
        }

        val builder = RenderUtils.MESH_BUILDER
        val emitter = TransformingQuadEmitter.Multi(
            builder.emitter, arrayOf(RotateQuadTransform(key.direction), SideQuadTransform(key.side))
        )

        RenderUtils.fromVanilla(backgroundModel, emitter, WRMaterials.UNPOWERED_MATERIAL)
        RenderUtils.fromVanilla(redstoneModel, emitter, material)

        return builder.build()
    }

    override fun emitQuads(key: GateDiodePartKey, ctx: PartRenderContext) {
        ctx.meshConsumer().accept(cache[key])
    }
}