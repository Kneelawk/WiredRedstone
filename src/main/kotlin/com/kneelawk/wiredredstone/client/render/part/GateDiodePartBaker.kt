package com.kneelawk.wiredredstone.client.render.part

import alexiil.mc.lib.multipart.api.render.PartModelBaker
import alexiil.mc.lib.multipart.api.render.PartRenderContext
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import com.kneelawk.wiredredstone.client.render.*
import com.kneelawk.wiredredstone.part.key.GateDiodePartKey
import io.vram.frex.api.model.BlockItemModel
import io.vram.frex.base.renderer.util.BakedModelTranscoder
import io.vram.frex.fabric.compat.FabricMesh
import net.minecraft.client.render.model.BasicBakedModel
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh as FAPIMesh

object GateDiodePartBaker : PartModelBaker<GateDiodePartKey> {
    private val cache: LoadingCache<GateDiodePartKey, FAPIMesh> =
        CacheBuilder.newBuilder().build(CacheLoader.from(::makeMesh))

    private fun makeMesh(key: GateDiodePartKey): FAPIMesh {
        val modelId = if (key.powered) {
            WRModels.GATE_DIODE_ON
        } else {
            WRModels.GATE_DIODE_OFF
        }

        val model = RenderUtils.getModel(modelId)

        val builder = RenderUtils.MESH_BUILDER
        val emitter = builder.emitter
            .withTransformQuad(AbsentBlockInputContext, SideQuadTransform(key.side))
            .withTransformQuad(AbsentBlockInputContext, RotateQuadTransform(key.direction))

        if (model is BasicBakedModel) {
            BakedModelTranscoder.accept(model, AbsentBlockInputContext, emitter)
        } else {
            (model as BlockItemModel).renderAsBlock(AbsentBlockInputContext, emitter)
        }

        val mesh = builder.build()

        return FabricMesh.of(mesh)
    }

    override fun emitQuads(key: GateDiodePartKey, ctx: PartRenderContext) {
        ctx.meshConsumer().accept(cache[key])
    }
}