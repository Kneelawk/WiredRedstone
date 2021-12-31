package com.kneelawk.wiredredstone.client.render.part

import alexiil.mc.lib.multipart.api.render.PartModelBaker
import alexiil.mc.lib.multipart.api.render.PartRenderContext
import com.kneelawk.wiredredstone.WRConstants
import com.kneelawk.wiredredstone.client.render.SideQuadTransform
import com.kneelawk.wiredredstone.part.key.RedAlloyWirePartKey
import net.fabricmc.fabric.api.renderer.v1.RendererAccess
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView
import net.minecraft.client.MinecraftClient
import net.minecraft.client.texture.SpriteAtlasTexture
import net.minecraft.util.math.Direction

object RedAlloyWirePartBaker : PartModelBaker<RedAlloyWirePartKey> {
    override fun emitQuads(key: RedAlloyWirePartKey, ctx: PartRenderContext) {
        ctx.pushTransform(SideQuadTransform(key.side))

        val emitter = ctx.emitter
        val material =
            RendererAccess.INSTANCE.renderer!!.materialFinder().blendMode(0, BlendMode.CUTOUT).emissive(0, true)
                .find()

        emitter.material(material)
        emitter.square(Direction.UP, 7f / 16f, 7f / 16f, 9f / 16f, 9f / 16f, 14f / 16f)
        emitter.spriteBake(0, MinecraftClient.getInstance().getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE)
            .apply(WRConstants.id("block/red_alloy_wire_powered")), MutableQuadView.BAKE_LOCK_UV
        )
        emitter.emit()

        ctx.popTransform()
    }
}