package com.kneelawk.wiredredstone.client.render.part

import alexiil.mc.lib.multipart.api.render.PartModelBaker
import alexiil.mc.lib.multipart.api.render.PartRenderContext
import com.kneelawk.wiredredstone.client.render.*
import com.kneelawk.wiredredstone.part.key.RedAlloyWirePartKey
import net.minecraft.util.math.Direction

object RedAlloyWirePartBaker : PartModelBaker<RedAlloyWirePartKey> {
    override fun emitQuads(key: RedAlloyWirePartKey, ctx: PartRenderContext) {
        ctx.pushTransform(SideQuadTransform(key.side))

        val sprite = if (key.powered) {
            WRSprites.RED_ALLOY_WIRE_POWERED_ID
        } else {
            WRSprites.RED_ALLOY_WIRE_UNPOWERED_ID
        }

        val material = if (key.powered) {
            WRMaterials.POWERED_MATERIAL
        } else {
            WRMaterials.UNPOWERED_MATERIAL
        }

        BoxEmitter.onGroundPixels(6f, 7f, 10f, 9f, 2f)
            .sprite(RenderUtils.getBlockSprite(sprite))
            .material(material)
            .downCullFace(Direction.DOWN)
            .sideTexCoordsCustomV(7f / 16f)
            .emit(ctx.emitter)

        ctx.popTransform()
    }
}