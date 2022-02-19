package com.kneelawk.wiredredstone.client.render

import com.kneelawk.wiredredstone.util.ConnectionUtils.isCorner
import com.kneelawk.wiredredstone.util.ConnectionUtils.isDisconnected
import com.kneelawk.wiredredstone.util.ConnectionUtils.isInternal
import com.kneelawk.wiredredstone.util.threadLocal
import io.vram.frex.api.buffer.QuadEmitter
import io.vram.frex.api.material.RenderMaterial
import io.vram.frex.api.mesh.MeshBuilder
import io.vram.frex.api.mesh.QuadView
import io.vram.frex.api.renderer.Renderer
import net.fabricmc.fabric.api.client.model.BakedModelManagerHelper
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.model.BakedModel
import net.minecraft.client.texture.Sprite
import net.minecraft.client.texture.SpriteAtlasTexture
import net.minecraft.util.Identifier
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Direction.*
import net.minecraft.util.math.Vec3f
import kotlin.math.sqrt

object RenderUtils {
    private const val WIRE_CLEARANCE = 0.001f

    val MESH_BUILDER: MeshBuilder by threadLocal {
        Renderer.get().meshBuilder()
    }

    fun getBlockSprite(id: Identifier): Sprite {
        return MinecraftClient.getInstance().getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE).apply(id)
    }

    fun getModel(id: Identifier): BakedModel {
        val manager = MinecraftClient.getInstance().bakedModelManager
        return BakedModelManagerHelper.getModel(manager, id) ?: manager.missingModel
    }

    fun emitWire(
        conn: UByte, axis: Axis, wireHeight: Float, wireWidth: Float, baseSprite: Sprite, endSprite: Sprite,
        sideV: Float, material: RenderMaterial, emitter: QuadEmitter
    ) {
        if (conn == 0u.toUByte()) {
            // no connections
            BoxEmitter.onGroundPixels(
                8f - wireWidth, 8f - wireWidth / 2f, 8f + wireWidth, 8f + wireWidth / 2f, wireHeight
            )
                .sprite(baseSprite)
                .westSprite(endSprite)
                .eastSprite(endSprite)
                .material(material)
                .downCullFace(DOWN)
                .setSideTexCoordsV(sideV)
                .emit(emitter)
        } else {
            val (doZNeg, zNegEnd) = calculateConnection(conn, wireWidth, NORTH, 0f)
            val (doXNeg, xNegEnd) = calculateConnection(conn, wireWidth, WEST, 0f)
            val (doZPos, zPosEnd) = calculateConnection(conn, wireWidth, SOUTH, 16f)
            val (doXPos, xPosEnd) = calculateConnection(conn, wireWidth, EAST, 16f)

            // We want wires along some axis to be slightly thinner so that they don't overlap
            val halfXWireWidth = if (axis != Axis.X) {
                wireWidth / 2f
            } else {
                wireWidth / 2f - WIRE_CLEARANCE
            }
            val halfZWireWidth = if (axis == Axis.Y) {
                wireWidth / 2f
            } else {
                wireWidth / 2f - WIRE_CLEARANCE
            }

            // Emit wire boxes

            if (doXNeg || doXPos) {
                BoxEmitter.onGroundPixels(xNegEnd, 8f - halfXWireWidth, xPosEnd, 8f + halfXWireWidth, wireHeight)
                    .sprite(baseSprite)
                    .westSprite(
                        if (isInternal(conn, WEST)) baseSprite else if (isCorner(conn, WEST)) null else endSprite
                    )
                    .eastSprite(
                        if (isInternal(conn, EAST)) baseSprite else if (isCorner(conn, EAST)) null else endSprite
                    )
                    .material(material)
                    .downCullFace(DOWN)
                    .setSideTexCoordsV(sideV)
                    .emit(emitter)
            }

            if (doZNeg || doZPos) {
                BoxEmitter.onGroundPixels(8f - halfZWireWidth, zNegEnd, 8f + halfZWireWidth, zPosEnd, wireHeight)
                    .sprite(baseSprite)
                    .northSprite(
                        if (isInternal(conn, NORTH)) baseSprite else if (isCorner(conn, NORTH)) null else endSprite
                    )
                    .southSprite(
                        if (isInternal(conn, SOUTH)) baseSprite else if (isCorner(conn, SOUTH)) null else endSprite
                    )
                    .material(material)
                    .downCullFace(DOWN)
                    .setSideTexCoordsV(sideV)
                    .emit(emitter)
            }

            // emit corner boxes

            if (isCorner(conn, NORTH)) {
                BoxEmitter.onGroundPixels(
                    8f - halfZWireWidth, -wireHeight + WIRE_CLEARANCE, 8f + halfZWireWidth, 0f, wireHeight
                )
                    .sprite(baseSprite)
                    .downSprite(null)
                    .southSprite(null)
                    .material(material)
                    .setSideTexCoordsV(sideV)
                    .translateDownTexCoords(0f, -1f)
                    .translateUpTexCoords(0f, 1f)
                    .translateWestTexCoords(1f, 0f)
                    .translateEastTexCoords(-1f, 0f)
                    .emit(emitter)
            }

            if (isCorner(conn, SOUTH)) {
                BoxEmitter.onGroundPixels(
                    8f - halfZWireWidth, 16f, 8f + halfZWireWidth, 16f + wireHeight - WIRE_CLEARANCE, wireHeight
                )
                    .sprite(baseSprite)
                    .downSprite(null)
                    .northSprite(null)
                    .material(material)
                    .setSideTexCoordsV(sideV)
                    .translateDownTexCoords(0f, 1f)
                    .translateUpTexCoords(0f, -1f)
                    .translateWestTexCoords(-1f, 0f)
                    .translateEastTexCoords(1f, 0f)
                    .emit(emitter)
            }

            if (isCorner(conn, WEST)) {
                BoxEmitter.onGroundPixels(
                    -wireHeight + WIRE_CLEARANCE, 8f - halfXWireWidth, 0f, 8f + halfXWireWidth, wireHeight
                )
                    .sprite(baseSprite)
                    .downSprite(null)
                    .eastSprite(null)
                    .material(material)
                    .setSideTexCoordsV(sideV)
                    .translateDownTexCoords(1f, 0f)
                    .translateUpTexCoords(1f, 0f)
                    .translateNorthTexCoords(-1f, 0f)
                    .translateSouthTexCoords(1f, 0f)
                    .emit(emitter)
            }

            if (isCorner(conn, EAST)) {
                BoxEmitter.onGroundPixels(
                    16f, 8f - halfXWireWidth, 16f + wireHeight - WIRE_CLEARANCE, 8f + halfXWireWidth, wireHeight
                )
                    .sprite(baseSprite)
                    .downSprite(null)
                    .westSprite(null)
                    .material(material)
                    .setSideTexCoordsV(sideV)
                    .translateDownTexCoords(-1f, 0f)
                    .translateUpTexCoords(-1f, 0f)
                    .translateNorthTexCoords(1f, 0f)
                    .translateSouthTexCoords(-1f, 0f)
                    .emit(emitter)
            }
        }
    }

    private fun calculateConnection(
        conn: UByte, wireWidth: Float, cardinal: Direction, externalEnd: Float
    ): Pair<Boolean, Float> {
        return if (!isDisconnected(conn, cardinal)) {
            Pair(true, externalEnd)
        } else {
            Pair(false, 8f + if (cardinal == NORTH || cardinal == WEST) -wireWidth / 2f else wireWidth / 2f)
        }
    }

    fun calculateFaceNormal(saveTo: Vec3f, q: QuadView) {
        // Get vertices
        val x0 = q.x(0)
        val y0 = q.y(0)
        val z0 = q.z(0)
        val x1 = q.x(1)
        val y1 = q.y(1)
        val z1 = q.z(1)
        val x2 = q.x(2)
        val y2 = q.y(2)
        val z2 = q.z(2)
        val x3 = q.x(3)
        val y3 = q.y(3)
        val z3 = q.z(3)

        // Calculate diagonal vectors
        val dx0 = x2 - x0
        val dy0 = y2 - y0
        val dz0 = z2 - z0
        val dx1 = x3 - x1
        val dy1 = y3 - y1
        val dz1 = z3 - z1

        // Cross diagonal vectors
        var normX = dy0 * dz1 - dz0 * dy1
        var normY = dz0 * dx1 - dx0 * dz1
        var normZ = dx0 * dy1 - dy0 * dx1

        // Normalize cross-product
        val l = sqrt(normX * normX + normY * normY + normZ * normZ)

        if (l != 0f) {
            normX /= l
            normY /= l
            normZ /= l
        }

        saveTo[normX, normY] = normZ
    }
}