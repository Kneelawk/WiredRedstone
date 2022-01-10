package com.kneelawk.wiredredstone.client.render.part

import alexiil.mc.lib.multipart.api.render.PartModelBaker
import alexiil.mc.lib.multipart.api.render.PartRenderContext
import com.kneelawk.wiredredstone.client.render.RenderUtils
import com.kneelawk.wiredredstone.client.render.RotateQuadTransform
import com.kneelawk.wiredredstone.client.render.SideQuadTransform
import com.kneelawk.wiredredstone.client.render.WRModels
import com.kneelawk.wiredredstone.part.key.GateDiodePartKey

object GateDiodePartBaker : PartModelBaker<GateDiodePartKey> {
    override fun emitQuads(key: GateDiodePartKey, ctx: PartRenderContext) {
        ctx.pushTransform(SideQuadTransform(key.side))
        ctx.pushTransform(RotateQuadTransform(key.direction))

        val modelId = if (key.powered) {
            WRModels.GATE_DIODE_ON
        } else {
            WRModels.GATE_DIODE_OFF
        }

        ctx.fallbackConsumer().accept(RenderUtils.getModel(modelId))

        ctx.popTransform()
        ctx.popTransform()
    }
}