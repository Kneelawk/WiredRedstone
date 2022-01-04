package com.kneelawk.wiredredstone.client.render.part

import alexiil.mc.lib.multipart.api.render.PartModelBaker
import alexiil.mc.lib.multipart.api.render.PartRenderContext
import com.kneelawk.wiredredstone.client.render.RenderUtils
import com.kneelawk.wiredredstone.client.render.SideQuadTransform
import com.kneelawk.wiredredstone.client.render.WRMaterials
import com.kneelawk.wiredredstone.client.render.WRSprites
import com.kneelawk.wiredredstone.part.key.RedAlloyWirePartKey

object RedAlloyWirePartBaker : PartModelBaker<RedAlloyWirePartKey> {
    override fun emitQuads(key: RedAlloyWirePartKey, ctx: PartRenderContext) {
        ctx.pushTransform(SideQuadTransform(key.side))

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

        RenderUtils.emitWire(key.connections, key.side.axis, 2f, 2f, sprite, sprite, 7f / 16f, material, ctx.emitter)

        ctx.popTransform()
    }
}