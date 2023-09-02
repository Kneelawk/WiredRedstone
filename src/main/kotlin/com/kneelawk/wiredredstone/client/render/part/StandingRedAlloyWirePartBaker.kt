package com.kneelawk.wiredredstone.client.render.part

import com.kneelawk.wiredredstone.client.render.RenderUtils
import com.kneelawk.wiredredstone.client.render.WRMaterials
import com.kneelawk.wiredredstone.client.render.WRSprites
import com.kneelawk.wiredredstone.client.render.WireRendering
import com.kneelawk.wiredredstone.part.key.StandingRedAlloyWirePartKey
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh

object StandingRedAlloyWirePartBaker : AbstractPartBaker<StandingRedAlloyWirePartKey>() {
    override fun makeMesh(key: StandingRedAlloyWirePartKey): Mesh {
        val spriteId = if (key.powered) {
            WRSprites.STANDING_RED_ALLOY_WIRE_POWERED_ID
        } else {
            WRSprites.STANDING_RED_ALLOY_WIRE_UNPOWERED_ID
        }

        val sprite = RenderUtils.getBlockSprite(spriteId)

        val material = if (key.powered) {
            WRMaterials.POWERED_MATERIAL
        } else {
            WRMaterials.UNPOWERED_MATERIAL
        }

        val builder = RenderUtils.MESH_BUILDER
        val emitter = builder.emitter

        WireRendering.emitCenterWire(
            conn = key.connections,
            wireDiameter = 2f / 16f,
            topCrossSprite = sprite,
            material = material,
            emitter = emitter
        )

        return builder.build()
    }
}
