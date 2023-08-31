package com.kneelawk.wiredredstone.client.render.part

import com.kneelawk.wiredredstone.client.render.BoxEmitter
import com.kneelawk.wiredredstone.client.render.RenderUtils
import com.kneelawk.wiredredstone.client.render.WRMaterials
import com.kneelawk.wiredredstone.client.render.WRSprites
import com.kneelawk.wiredredstone.part.key.StandingRedAlloyWirePartKey
import com.kneelawk.wiredredstone.util.bits.CenterConnectionUtils.test
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter
import net.minecraft.client.texture.Sprite
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Direction.*

object StandingRedAlloyWirePartBaker : AbstractPartBaker<StandingRedAlloyWirePartKey>() {
    private const val WIRE_CLEARANCE = 0.0005f

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

        emitCenterWire(
            conn = key.connections,
            wireDiameter = 2f / 16f,
            crossSprite = sprite,
            material = material,
            emitter = emitter
        )

        return builder.build()
    }

    fun emitCenterWire(
        conn: UByte, wireDiameter: Float, crossSprite: Sprite, verticalSprite: Sprite = crossSprite,
        horizontalSprite: Sprite = crossSprite, endSprite: Sprite = crossSprite, endDecalSprite: Sprite? = null,
        material: RenderMaterial, decalMaterial: RenderMaterial? = null, emitter: QuadEmitter
    ) {
        if (conn == 0u.toUByte()) {
            BoxEmitter.of(
                0.5f - wireDiameter / 2f, 0.5f - wireDiameter / 2f, 0.5f - wireDiameter / 2f, 0.5f + wireDiameter / 2f,
                0.5f + wireDiameter / 2f, 0.5f + wireDiameter / 2f
            )
                .downSprite(horizontalSprite)
                .upSprite(horizontalSprite)
                .northSprite(horizontalSprite)
                .southSprite(horizontalSprite)
                .westSprite(endSprite)
                .eastSprite(endSprite)
                .westDecal1(endDecalSprite)
                .eastDecal1(endDecalSprite)
                .material(material)
                .decal1Material(decalMaterial)
                .emit(emitter)
        } else {
            val (doYNeg, yNegEnd) = calculateConnection(conn, wireDiameter, DOWN, 0f)
            val (doZNeg, zNegEnd) = calculateConnection(conn, wireDiameter, NORTH, 0f)
            val (doXNeg, xNegEnd) = calculateConnection(conn, wireDiameter, WEST, 0f)
            val (doYPos, yPosEnd) = calculateConnection(conn, wireDiameter, UP, 1f)
            val (doZPos, zPosEnd) = calculateConnection(conn, wireDiameter, SOUTH, 1f)
            val (doXPos, xPosEnd) = calculateConnection(conn, wireDiameter, EAST, 1f)

            // Emit wire boxes

            if (doYNeg || doYPos) {
                val others = doZNeg || doXNeg || doZPos || doXPos
                BoxEmitter.of(
                    0.5f - wireDiameter / 2f, yNegEnd, 0.5f - wireDiameter / 2f,
                    0.5f + wireDiameter / 2f, yPosEnd, 0.5f + wireDiameter / 2f
                )
                    .widenX(WIRE_CLEARANCE * 2f)
                    .widenZ(WIRE_CLEARANCE)
                    .extendDown(if (doYNeg || !others) 0f else WIRE_CLEARANCE * 2f)
                    .extendUp(if (doYPos || !others) 0f else WIRE_CLEARANCE * 2f)
                    .downSprite(if (doYNeg || !others) endSprite else null)
                    .downDecal1(if (doYNeg || !others) endDecalSprite else null)
                    .upSprite(if (doYPos || !others) endSprite else null)
                    .upDecal1(if (doYPos || !others) endDecalSprite else null)
                    .northSprite(if (doXNeg || doXPos) crossSprite else verticalSprite)
                    .southSprite(if (doXNeg || doXPos) crossSprite else verticalSprite)
                    .westSprite(if (doZNeg || doZPos) crossSprite else verticalSprite)
                    .eastSprite(if (doZNeg || doZPos) crossSprite else verticalSprite)
                    .material(material)
                    .decal1Material(decalMaterial)
                    .emit(emitter)
            }

            if (doZNeg || doZPos) {
                val others = doYNeg || doXNeg || doYPos || doXPos
                BoxEmitter.of(
                    0.5f - wireDiameter / 2f, 0.5f - wireDiameter / 2f, zNegEnd,
                    0.5f + wireDiameter / 2f, 0.5f + wireDiameter / 2f, zPosEnd
                )
                    .widenX(WIRE_CLEARANCE)
                    .widenY(WIRE_CLEARANCE * 2f)
                    .extendNorth(if (doZNeg || !others) 0f else WIRE_CLEARANCE * 2f)
                    .extendSouth(if (doZPos || !others) 0f else WIRE_CLEARANCE * 2f)
                    .downSprite(if (doXNeg || doXPos) crossSprite else verticalSprite)
                    .upSprite(if (doXNeg || doXPos) crossSprite else verticalSprite)
                    .northSprite(if (doZNeg || !others) endSprite else null)
                    .northDecal1(if (doZNeg || !others) endDecalSprite else null)
                    .southSprite(if (doZPos || !others) endSprite else null)
                    .southDecal1(if (doZPos || !others) endDecalSprite else null)
                    .westSprite(if (doYNeg || doYPos) crossSprite else horizontalSprite)
                    .eastSprite(if (doYNeg || doYPos) crossSprite else horizontalSprite)
                    .material(material)
                    .decal1Material(decalMaterial)
                    .emit(emitter)
            }

            if (doXNeg || doXPos) {
                val others = doYNeg || doZNeg || doYPos || doZPos
                BoxEmitter.of(
                    xNegEnd, 0.5f - wireDiameter / 2f, 0.5f - wireDiameter / 2f,
                    xPosEnd, 0.5f + wireDiameter / 2f, 0.5f + wireDiameter / 2f
                )
                    .widenY(WIRE_CLEARANCE)
                    .widenZ(WIRE_CLEARANCE * 2f)
                    .extendWest(if (doXNeg || !others) 0f else WIRE_CLEARANCE * 2f)
                    .extendEast(if (doXPos || !others) 0f else WIRE_CLEARANCE * 2f)
                    .downSprite(if (doZNeg || doZPos) crossSprite else horizontalSprite)
                    .upSprite(if (doZNeg || doZPos) crossSprite else horizontalSprite)
                    .northSprite(if (doYNeg || doYPos) crossSprite else horizontalSprite)
                    .southSprite(if (doYNeg || doYPos) crossSprite else horizontalSprite)
                    .westSprite(if (doXNeg || !others) endSprite else null)
                    .westDecal1(if (doXNeg || !others) endDecalSprite else null)
                    .eastSprite(if (doXPos || !others) endSprite else null)
                    .eastDecal1(if (doXPos || !others) endDecalSprite else null)
                    .material(material)
                    .decal1Material(decalMaterial)
                    .emit(emitter)
            }
        }
    }

    private fun calculateConnection(
        conn: UByte, wireDiameter: Float, dir: Direction, externalEnd: Float
    ): Pair<Boolean, Float> {
        return if (test(conn, dir)) {
            Pair(true, externalEnd)
        } else {
            Pair(
                false, 0.5f + if (dir == DOWN || dir == NORTH || dir == WEST) -wireDiameter / 2f else wireDiameter / 2f
            )
        }
    }
}
