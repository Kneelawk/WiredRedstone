package com.kneelawk.wiredredstone.client.render.part

import com.kneelawk.wiredredstone.WRConstants
import com.kneelawk.wiredredstone.WRConstants.id
import com.kneelawk.wiredredstone.client.render.RenderUtils
import com.kneelawk.wiredredstone.client.render.WRMaterials
import com.kneelawk.wiredredstone.client.render.WireRendering
import com.kneelawk.wiredredstone.part.key.StandingInsulatedWirePartKey
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh
import net.minecraft.client.texture.Sprite
import net.minecraft.util.DyeColor
import net.minecraft.util.Identifier

object StandingInsulatedWirePartBaker : AbstractPartBaker<StandingInsulatedWirePartKey>() {
    override fun makeMesh(key: StandingInsulatedWirePartKey): Mesh {
        val builder = RenderUtils.MESH_BUILDER
        val emitter = builder.emitter

        val endSpriteId = if (key.powered) STANDING_INSULATED_WIRE_END_POWERED_ID else null
        val endSprite = endSpriteId?.let(RenderUtils::getBlockSprite)
        val endMaterial = if (key.powered) WRMaterials.POWERED_MATERIAL else null

        val sprites = STANDING_INSULATED_WIRE_IDS[key.color]!!.lookup()

        WireRendering.emitCenterWire(
            conn = key.connections,
            wireDiameter = 4f / 16f,
            topCrossSprite = sprites.cross,
            topXSprite = sprites.x,
            topZSprite = sprites.z,
            endSprite = sprites.end,
            endDecalSprite = endSprite,
            material = WRMaterials.UNPOWERED_MATERIAL,
            decalMaterial = endMaterial,
            emitter = emitter
        )

        return builder.build()
    }

    data class WireIds(
        val cross: Identifier, val x: Identifier, val z: Identifier,
        val end: Identifier
    ) {
        fun lookup(): WireSprites {
            return WireSprites(
                RenderUtils.getBlockSprite(cross),
                RenderUtils.getBlockSprite(x),
                RenderUtils.getBlockSprite(z),
                RenderUtils.getBlockSprite(end)
            )
        }
    }

    data class WireSprites(
        val cross: Sprite, val x: Sprite, val z: Sprite, val end: Sprite
    )

    private val STANDING_INSULATED_WIRE_END_POWERED_ID = WRConstants.id("block/standing_insulated_wire/end_powered")
    private val STANDING_INSULATED_WIRE_IDS = DyeColor.values().associateWith {
        WireIds(
            id("block/standing_insulated_wire/${it.getName()}_cross"),
            id("block/standing_insulated_wire/${it.getName()}_x"),
            id("block/standing_insulated_wire/${it.getName()}_z"),
            id("block/standing_insulated_wire/${it.getName()}_end")
        )
    }
}
