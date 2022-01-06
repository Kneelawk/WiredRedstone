package com.kneelawk.wiredredstone.client.render

import com.kneelawk.wiredredstone.WRLog
import com.kneelawk.wiredredstone.util.ConnectionUtils.isCorner
import com.kneelawk.wiredredstone.util.ConnectionUtils.isExternal
import com.kneelawk.wiredredstone.util.ConnectionUtils.isInternal
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadView
import net.minecraft.client.MinecraftClient
import net.minecraft.client.texture.Sprite
import net.minecraft.client.texture.SpriteAtlasTexture
import net.minecraft.util.Identifier
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Direction.*
import net.minecraft.util.math.Vec3f
import kotlin.math.sqrt

object RenderUtils {
    fun getBlockSprite(id: Identifier): Sprite {
        return MinecraftClient.getInstance().getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE).apply(id)
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
            val (doZNeg, zNegEnd) = calculateConnection(
                conn, axis, wireWidth, NORTH, wireHeight, 0f, Axis.Y, true
            )
            val (doXNeg, xNegEnd) = calculateConnection(
                conn, axis, wireWidth, WEST, wireHeight, 0f, Axis.X, false
            )
            val (doZPos, zPosEnd) = calculateConnection(
                conn, axis, wireWidth, SOUTH, 16f - wireHeight, 16f, Axis.Y, true
            )
            val (doXPos, xPosEnd) = calculateConnection(
                conn, axis, wireWidth, EAST, 16f - wireHeight, 16f, Axis.X, false
            )

            // Emit wire boxes

            if (doXNeg || doXPos) {
                BoxEmitter.onGroundPixels(xNegEnd, 8f - wireWidth / 2f, xPosEnd, 8f + wireWidth / 2f, wireHeight)
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
                BoxEmitter.onGroundPixels(8f - wireWidth / 2f, zNegEnd, 8f + wireWidth / 2f, zPosEnd, wireHeight)
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

            if (isCorner(conn, NORTH) && axis == Axis.Y) {
                BoxEmitter.onGroundPixels(8f - wireWidth / 2f, -wireHeight, 8f + wireWidth / 2f, 0f, wireHeight)
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

            if (isCorner(conn, SOUTH) && axis == Axis.Y) {
                BoxEmitter.onGroundPixels(8f - wireWidth / 2f, 16f, 8f + wireWidth / 2f, 16f + wireHeight, wireHeight)
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

            if (isCorner(conn, WEST) && axis != Axis.X) {
                BoxEmitter.onGroundPixels(-wireHeight, 8f - wireWidth / 2f, 0f, 8f + wireWidth / 2f, wireHeight)
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

            if (isCorner(conn, EAST) && axis != Axis.X) {
                BoxEmitter.onGroundPixels(16f, 8f - wireWidth / 2f, 16f + wireHeight, 8f + wireWidth / 2f, wireHeight)
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
        conn: UByte, axis: Axis, wireWidth: Float, cardinal: Direction, internalEnd: Float, externalEnd: Float,
        specialAxis: Axis, axisIsLarger: Boolean
    ): Pair<Boolean, Float> {
        return if (isInternal(conn, cardinal)) {
            Pair(true, if ((axis == specialAxis) == axisIsLarger) externalEnd else internalEnd)
        } else if (isExternal(conn, cardinal) || isCorner(conn, cardinal)) {
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