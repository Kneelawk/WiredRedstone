package com.kneelawk.wiredredstone.client.render.part

import alexiil.mc.lib.multipart.api.render.PartModelBaker
import alexiil.mc.lib.multipart.api.render.PartRenderContext
import com.kneelawk.wiredredstone.client.render.*
import com.kneelawk.wiredredstone.part.key.GateDiodePartKey
import io.vram.frex.api.model.BlockItemModel
import io.vram.frex.base.renderer.util.BakedModelTranscoder
import io.vram.frex.fabric.compat.FabricMesh
import net.minecraft.client.render.model.BasicBakedModel

object GateDiodePartBaker : PartModelBaker<GateDiodePartKey> {
    override fun emitQuads(key: GateDiodePartKey, ctx: PartRenderContext) {
        ctx.pushTransform(SideQuadTransform(key.side))
        ctx.pushTransform(RotateQuadTransform(key.direction))

        val modelId = if (key.powered) {
            WRModels.GATE_DIODE_ON
        } else {
            WRModels.GATE_DIODE_OFF
        }

        val model = RenderUtils.getModel(modelId)

        val builder = RenderUtils.MESH_BUILDER
        val emitter = builder.emitter

        if (model is BasicBakedModel) {
            BakedModelTranscoder.accept(model, AbsentBlockInputContext, emitter)
        } else {
            (model as BlockItemModel).renderAsBlock(AbsentBlockInputContext, emitter)
        }

        val mesh = builder.build()

        ctx.meshConsumer().accept(FabricMesh.of(mesh))

        ctx.popTransform()
        ctx.popTransform()
    }
}