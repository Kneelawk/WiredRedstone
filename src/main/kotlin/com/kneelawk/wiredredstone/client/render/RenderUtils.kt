package com.kneelawk.wiredredstone.client.render

import com.kneelawk.wiredredstone.util.ConnectionUtils.isCorner
import com.kneelawk.wiredredstone.util.ConnectionUtils.isDisconnected
import com.kneelawk.wiredredstone.util.ConnectionUtils.isInternal
import com.kneelawk.wiredredstone.util.requireNonNull
import com.kneelawk.wiredredstone.util.threadLocal
import net.fabricmc.fabric.api.client.model.BakedModelManagerHelper
import net.fabricmc.fabric.api.renderer.v1.RendererAccess
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadView
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.model.BakedModel
import net.minecraft.client.texture.Sprite
import net.minecraft.client.texture.SpriteAtlasTexture
import net.minecraft.util.Identifier
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Direction.*
import net.minecraft.util.math.Vec3f
import java.util.*
import kotlin.math.sqrt

object RenderUtils {
    private const val WIRE_CLEARANCE = 0.001f

    val MESH_BUILDER: MeshBuilder by threadLocal {
        RendererAccess.INSTANCE.renderer.requireNonNull("Renderer is null").meshBuilder()
    }

    fun getBlockSprite(id: Identifier): Sprite {
        return MinecraftClient.getInstance().getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE).apply(id)
    }

    fun getModel(id: Identifier): BakedModel {
        val manager = MinecraftClient.getInstance().bakedModelManager
        return BakedModelManagerHelper.getModel(manager, id) ?: manager.missingModel
    }

    fun emitWire(
        conn: UByte, axis: Axis, wireHeight: Float, wireWidth: Float, topCrossSprite: Sprite,
        topXSprite: Sprite = topCrossSprite, topZSprite: Sprite = topCrossSprite, sideSprite: Sprite = topCrossSprite,
        openEndSprite: Sprite = topZSprite, closedEndSprite: Sprite = topZSprite, sideV: Float,
        material: RenderMaterial, emitter: QuadEmitter
    ) {
        if (conn == 0u.toUByte()) {
            // no connections
            BoxEmitter.onGroundPixels(
                8f - wireWidth, 8f - wireWidth / 2f, 8f + wireWidth, 8f + wireWidth / 2f, wireHeight
            )
                .sprite(sideSprite)
                .upSprite(topXSprite)
                .westSprite(openEndSprite)
                .eastSprite(openEndSprite)
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
            val halfXWireWidth = halfXWireWidth(axis, wireWidth)
            val halfZWireWidth = halfZWireWidth(axis, wireWidth)

            // Emit wire boxes

            if (doXNeg || doXPos) {
                BoxEmitter.onGroundPixels(xNegEnd, 8f - halfXWireWidth, xPosEnd, 8f + halfXWireWidth, wireHeight)
                    .sprite(sideSprite)
                    .upSprite(if (!(doZNeg || doZPos)) topXSprite else topCrossSprite)
                    .westSprite(
                        if (isInternal(conn, WEST)) {
                            sideSprite
                        } else if (isCorner(conn, WEST) || (isDisconnected(conn, WEST) && (doZNeg || doZPos))) {
                            null
                        } else openEndSprite
                    )
                    .eastSprite(
                        if (isInternal(conn, EAST)) {
                            sideSprite
                        } else if (isCorner(conn, EAST) || (isDisconnected(conn, EAST) && (doZNeg || doZPos))) {
                            null
                        } else openEndSprite
                    )
                    .material(material)
                    .downCullFace(DOWN)
                    .setSideTexCoordsV(sideV)
                    .emit(emitter)
            }

            if (doZNeg || doZPos) {
                BoxEmitter.onGroundPixels(8f - halfZWireWidth, zNegEnd, 8f + halfZWireWidth, zPosEnd, wireHeight)
                    .sprite(sideSprite)
                    .upSprite(if (!(doXNeg || doXPos)) topZSprite else topCrossSprite)
                    .northSprite(
                        if (isInternal(conn, NORTH)) {
                            sideSprite
                        } else if (isCorner(conn, NORTH) || (isDisconnected(conn, NORTH) && (doXNeg || doXPos))) {
                            null
                        } else openEndSprite
                    )
                    .southSprite(
                        if (isInternal(conn, SOUTH)) {
                            sideSprite
                        } else if (isCorner(conn, SOUTH) || (isDisconnected(conn, SOUTH) && (doXNeg || doXPos))) {
                            null
                        } else openEndSprite
                    )
                    .material(material)
                    .downCullFace(DOWN)
                    .setSideTexCoordsV(sideV)
                    .emit(emitter)
            }

            // emit corner boxes

            emitNorthWireCorner(
                conn, wireHeight, halfZWireWidth, sideSprite, topZSprite, openEndSprite, closedEndSprite, sideV,
                material, emitter
            )
            emitSouthWireCorner(
                conn, wireHeight, halfZWireWidth, sideSprite, topZSprite, openEndSprite, closedEndSprite, sideV,
                material, emitter
            )
            emitWestWireCorner(
                conn, wireHeight, halfXWireWidth, sideSprite, topXSprite, openEndSprite, closedEndSprite, sideV,
                material, emitter
            )
            emitEastWireCorner(
                conn, wireHeight, halfXWireWidth, sideSprite, topXSprite, openEndSprite, closedEndSprite, sideV,
                material, emitter
            )
        }
    }

    fun emitNorthWireCorner(
        conn: UByte, sideAxis: Axis, rotationAxis: Axis, wireHeight: Float, wireWidth: Float, sprite: Sprite, sideV: Float,
        material: RenderMaterial, emitter: QuadEmitter
    ) {
        emitNorthWireCorner(
            conn, wireHeight, if (rotationAxis == Axis.Z) halfZWireWidth(sideAxis, wireWidth) else halfXWireWidth(sideAxis, wireWidth), sprite, sprite, sprite, sprite, sideV, material, emitter
        )
    }

    fun emitSouthWireCorner(
        conn: UByte, sideAxis: Axis, rotationAxis: Axis, wireHeight: Float, wireWidth: Float, sprite: Sprite, sideV: Float,
        material: RenderMaterial, emitter: QuadEmitter
    ) {
        emitSouthWireCorner(
            conn, wireHeight, if (rotationAxis == Axis.Z) halfZWireWidth(sideAxis, wireWidth) else halfXWireWidth(sideAxis, wireWidth), sprite, sprite, sprite, sprite, sideV, material, emitter
        )
    }

    fun emitWestWireCorner(
        conn: UByte, sideAxis: Axis, rotationAxis: Axis, wireHeight: Float, wireWidth: Float, sprite: Sprite, sideV: Float,
        material: RenderMaterial, emitter: QuadEmitter
    ) {
        emitWestWireCorner(
            conn, wireHeight, if (rotationAxis == Axis.Z) halfXWireWidth(sideAxis, wireWidth) else halfZWireWidth(sideAxis, wireWidth), sprite, sprite, sprite, sprite, sideV, material, emitter
        )
    }

    fun emitEastWireCorner(
        conn: UByte, sideAxis: Axis, rotationAxis: Axis, wireHeight: Float, wireWidth: Float, sprite: Sprite, sideV: Float,
        material: RenderMaterial, emitter: QuadEmitter
    ) {
        emitEastWireCorner(
            conn, wireHeight, if (rotationAxis == Axis.Z) halfXWireWidth(sideAxis, wireWidth) else halfZWireWidth(sideAxis, wireWidth), sprite, sprite, sprite, sprite, sideV, material, emitter
        )
    }

    private fun emitNorthWireCorner(
        conn: UByte, wireHeight: Float, halfZWireWidth: Float, sideSprite: Sprite, topZSprite: Sprite,
        openEndSprite: Sprite, closedEndSprite: Sprite, sideV: Float, material: RenderMaterial, emitter: QuadEmitter
    ) {
        if (isCorner(conn, NORTH)) {
            BoxEmitter.onGroundPixels(
                8f - halfZWireWidth, -wireHeight + WIRE_CLEARANCE, 8f + halfZWireWidth, 0f, wireHeight
            )
                .sprite(sideSprite)
                .upSprite(topZSprite)
                .northSprite(closedEndSprite)
                .downSprite(openEndSprite)
                .southSprite(null)
                .material(material)
                .setSideTexCoordsV(sideV)
                .translateDownTexCoords(0f, sideV - 1f)
                .translateUpTexCoords(0f, 1f)
                .translateWestTexCoords(1f, 0f)
                .translateEastTexCoords(-1f, 0f)
                .downFlipV()
                .emit(emitter)
        }
    }

    private fun emitSouthWireCorner(
        conn: UByte, wireHeight: Float, halfZWireWidth: Float, sideSprite: Sprite, topZSprite: Sprite,
        openEndSprite: Sprite, closedEndSprite: Sprite, sideV: Float, material: RenderMaterial, emitter: QuadEmitter
    ) {
        if (isCorner(conn, SOUTH)) {
            BoxEmitter.onGroundPixels(
                8f - halfZWireWidth, 16f, 8f + halfZWireWidth, 16f + wireHeight - WIRE_CLEARANCE, wireHeight
            )
                .sprite(sideSprite)
                .upSprite(topZSprite)
                .southSprite(closedEndSprite)
                .downSprite(openEndSprite)
                .northSprite(null)
                .material(material)
                .setSideTexCoordsV(sideV)
                .translateDownTexCoords(0f, sideV + wireHeight / 16f)
                .translateUpTexCoords(0f, -1f)
                .translateWestTexCoords(-1f, 0f)
                .translateEastTexCoords(1f, 0f)
                .emit(emitter)
        }
    }

    private fun emitWestWireCorner(
        conn: UByte, wireHeight: Float, halfXWireWidth: Float, sideSprite: Sprite, topXSprite: Sprite,
        openEndSprite: Sprite, closedEndSprite: Sprite, sideV: Float, material: RenderMaterial, emitter: QuadEmitter
    ) {
        if (isCorner(conn, WEST)) {
            BoxEmitter.onGroundPixels(
                -wireHeight + WIRE_CLEARANCE, 8f - halfXWireWidth, 0f, 8f + halfXWireWidth, wireHeight
            )
                .sprite(sideSprite)
                .upSprite(topXSprite)
                .westSprite(closedEndSprite)
                .downSprite(openEndSprite)
                .eastSprite(null)
                .material(material)
                .setSideTexCoordsV(sideV)
                .downTexCoords(0.5f - halfXWireWidth / 16f, sideV)
                .translateUpTexCoords(1f, 0f)
                .translateNorthTexCoords(-1f, 0f)
                .translateSouthTexCoords(1f, 0f)
                .downRotation(BoxEmitter.Rotation.DEGREES_270)
                .emit(emitter)
        }
    }

    private fun emitEastWireCorner(
        conn: UByte, wireHeight: Float, halfXWireWidth: Float, sideSprite: Sprite, topXSprite: Sprite,
        openEndSprite: Sprite, closedEndSprite: Sprite, sideV: Float, material: RenderMaterial, emitter: QuadEmitter
    ) {
        if (isCorner(conn, EAST)) {
            BoxEmitter.onGroundPixels(
                16f, 8f - halfXWireWidth, 16f + wireHeight - WIRE_CLEARANCE, 8f + halfXWireWidth, wireHeight
            )
                .sprite(sideSprite)
                .upSprite(topXSprite)
                .eastSprite(closedEndSprite)
                .downSprite(openEndSprite)
                .westSprite(null)
                .material(material)
                .setSideTexCoordsV(sideV)
                .downTexCoords(0.5f - halfXWireWidth / 16f, sideV)
                .translateUpTexCoords(-1f, 0f)
                .translateNorthTexCoords(1f, 0f)
                .translateSouthTexCoords(-1f, 0f)
                .downRotation(BoxEmitter.Rotation.DEGREES_90)
                .emit(emitter)
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

    private fun halfZWireWidth(axis: Axis, wireWidth: Float) =
        if (axis == Axis.Y) {
            wireWidth / 2f
        } else {
            wireWidth / 2f - WIRE_CLEARANCE
        }

    private fun halfXWireWidth(axis: Axis, wireWidth: Float) =
        if (axis != Axis.X) {
            wireWidth / 2f
        } else {
            wireWidth / 2f - WIRE_CLEARANCE
        }

    fun fromVanilla(from: BakedModel, to: QuadEmitter, material: RenderMaterial) {
        val random = Random(42)

        for (dir in Direction.values()) {
            val quads = from.getQuads(null, dir, random)
            for (quad in quads) {
                to.fromVanilla(quad, material, dir)
                to.emit()
            }
        }

        val quads = from.getQuads(null, null, random)
        for (quad in quads) {
            to.fromVanilla(quad, material, null)
            to.emit()
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