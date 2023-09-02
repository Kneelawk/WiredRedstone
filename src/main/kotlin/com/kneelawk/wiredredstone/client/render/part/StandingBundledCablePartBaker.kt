package com.kneelawk.wiredredstone.client.render.part

import com.kneelawk.wiredredstone.WRConstants.id
import com.kneelawk.wiredredstone.client.render.RenderUtils
import com.kneelawk.wiredredstone.client.render.WRMaterials
import com.kneelawk.wiredredstone.client.render.WireRendering
import com.kneelawk.wiredredstone.part.key.StandingBundledCablePartKey
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh
import net.minecraft.client.texture.Sprite
import net.minecraft.util.DyeColor
import net.minecraft.util.Identifier

object StandingBundledCablePartBaker : AbstractPartBaker<StandingBundledCablePartKey>() {
    override fun makeMesh(key: StandingBundledCablePartKey): Mesh {
        val builder = RenderUtils.MESH_BUILDER
        val emitter = builder.emitter

        val sprites = STANDING_BUNDLED_CABLE_IDS[key.color]!!.lookup()

        WireRendering.emitCenterWire(
            conn = key.connections,
            wireDiameter = 6f / 16f,
            topCrossSprite = sprites.topCross,
            topXSprite = sprites.topX,
            topZSprite = sprites.topZ,
            bottomCrossSprite = sprites.bottomCross,
            bottomXSprite = sprites.bottomX,
            bottomZSprite = sprites.bottomZ,
            lowerCrossSprite = sprites.lowerCross,
            lowerXSprite = sprites.lowerX,
            lowerZSprite = sprites.lowerZ,
            upperCrossSprite = sprites.upperCross,
            upperXSprite = sprites.upperX,
            upperZSprite = sprites.upperZ,
            endSprite = sprites.end,
            material = WRMaterials.UNPOWERED_MATERIAL,
            emitter = emitter
        )

        return builder.build()
    }

    data class WireIds(
        val topCross: Identifier, val topX: Identifier, val topZ: Identifier, val bottomCross: Identifier,
        val bottomX: Identifier, val bottomZ: Identifier, val lowerCross: Identifier, val lowerX: Identifier,
        val lowerZ: Identifier, val upperCross: Identifier, val upperX: Identifier, val upperZ: Identifier,
        val end: Identifier
    ) {
        fun lookup(): WireSprites {
            return WireSprites(
                RenderUtils.getBlockSprite(topCross),
                RenderUtils.getBlockSprite(topX),
                RenderUtils.getBlockSprite(topZ),
                RenderUtils.getBlockSprite(bottomCross),
                RenderUtils.getBlockSprite(bottomX),
                RenderUtils.getBlockSprite(bottomZ),
                RenderUtils.getBlockSprite(lowerCross),
                RenderUtils.getBlockSprite(lowerX),
                RenderUtils.getBlockSprite(lowerZ),
                RenderUtils.getBlockSprite(upperCross),
                RenderUtils.getBlockSprite(upperX),
                RenderUtils.getBlockSprite(upperZ),
                RenderUtils.getBlockSprite(end),
            )
        }
    }

    data class WireSprites(
        val topCross: Sprite, val topX: Sprite, val topZ: Sprite, val bottomCross: Sprite,
        val bottomX: Sprite, val bottomZ: Sprite, val lowerCross: Sprite, val lowerX: Sprite,
        val lowerZ: Sprite, val upperCross: Sprite, val upperX: Sprite, val upperZ: Sprite,
        val end: Sprite
    )

    private val STANDING_BUNDLED_CABLE_IDS = (listOf<DyeColor?>(null) + DyeColor.values()).associateWith { color ->
        val path = "block/standing_bundled_cable" + (color?.let { "/" + it.getName() } ?: "")

        WireIds(
            id("$path/top_cross"),
            id("$path/top_x"),
            id("$path/top_z"),
            id("$path/bottom_cross"),
            id("$path/bottom_x"),
            id("$path/bottom_z"),
            id("$path/lower_cross"),
            id("$path/lower_x"),
            id("$path/lower_z"),
            id("$path/upper_cross"),
            id("$path/upper_x"),
            id("$path/upper_z"),
            id("$path/end")
        )
    }
}
