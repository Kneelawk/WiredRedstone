package com.kneelawk.wiredredstone.client.render

import com.kneelawk.wiredredstone.util.ConnectionUtils
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter
import net.minecraft.client.texture.Sprite
import net.minecraft.util.math.Direction

object WireRendering {
    private const val WIRE_CLEARANCE = 0.001f

    fun emitWire(
        conn: UByte, axis: Direction.Axis, wireHeight: Float, wireWidth: Float, topCrossSprite: Sprite,
        topXSprite: Sprite = topCrossSprite, topZSprite: Sprite = topCrossSprite, sideSprite: Sprite = topCrossSprite,
        openEndSprite: Sprite = topZSprite, closedEndSprite: Sprite = topZSprite, openEndDecal: Sprite? = null,
        sideV: Float, material: RenderMaterial, decalMaterial: RenderMaterial? = null, emitter: QuadEmitter
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
                .westDecal1(openEndDecal)
                .eastDecal1(openEndDecal)
                .material(material)
                .downCullFace(Direction.DOWN)
                .setSideTexCoordsV(sideV)
                .emit(emitter)
        } else {
            val (doZNeg, zNegEnd) = calculateConnection(conn, wireWidth, Direction.NORTH, 0f)
            val (doXNeg, xNegEnd) = calculateConnection(conn, wireWidth, Direction.WEST, 0f)
            val (doZPos, zPosEnd) = calculateConnection(conn, wireWidth, Direction.SOUTH, 16f)
            val (doXPos, xPosEnd) = calculateConnection(conn, wireWidth, Direction.EAST, 16f)

            // We want wires along some axis to be slightly thinner so that they don't overlap
            val halfXWireWidth = halfXWireWidth(axis, wireWidth)
            val halfZWireWidth = halfZWireWidth(axis, wireWidth)

            // Emit wire boxes

            if (doXNeg || doXPos) {
                BoxEmitter.onGroundPixels(xNegEnd, 8f - halfXWireWidth, xPosEnd, 8f + halfXWireWidth, wireHeight)
                    .sprite(sideSprite)
                    .upSprite(if (!(doZNeg || doZPos)) topXSprite else topCrossSprite)
                    .westSprite(
                        if (ConnectionUtils.isInternal(conn, Direction.WEST)) {
                            sideSprite
                        } else if (ConnectionUtils.isCorner(conn, Direction.WEST) || (ConnectionUtils.isDisconnected(
                                conn, Direction.WEST
                            ) && (doZNeg || doZPos))) {
                            null
                        } else openEndSprite
                    )
                    .eastSprite(
                        if (ConnectionUtils.isInternal(conn, Direction.EAST)) {
                            sideSprite
                        } else if (ConnectionUtils.isCorner(conn, Direction.EAST) || (ConnectionUtils.isDisconnected(
                                conn, Direction.EAST
                            ) && (doZNeg || doZPos))) {
                            null
                        } else openEndSprite
                    )
                    .westDecal1(
                        if (ConnectionUtils.isExternal(conn, Direction.WEST) || (ConnectionUtils.isDisconnected(
                                conn, Direction.WEST
                            ) && !(doZNeg || doZPos))) {
                            openEndDecal
                        } else null
                    )
                    .eastDecal1(
                        if (ConnectionUtils.isExternal(conn, Direction.EAST) || (ConnectionUtils.isDisconnected(
                                conn, Direction.EAST
                            ) && !(doZNeg || doZPos))) {
                            openEndDecal
                        } else null
                    )
                    .material(material)
                    .decal1Material(decalMaterial)
                    .downCullFace(Direction.DOWN)
                    .setSideTexCoordsV(sideV)
                    .emit(emitter)
            }

            if (doZNeg || doZPos) {
                BoxEmitter.onGroundPixels(8f - halfZWireWidth, zNegEnd, 8f + halfZWireWidth, zPosEnd, wireHeight)
                    .sprite(sideSprite)
                    .upSprite(if (!(doXNeg || doXPos)) topZSprite else topCrossSprite)
                    .northSprite(
                        if (ConnectionUtils.isInternal(conn, Direction.NORTH)) {
                            sideSprite
                        } else if (ConnectionUtils.isCorner(conn, Direction.NORTH) || (ConnectionUtils.isDisconnected(
                                conn, Direction.NORTH
                            ) && (doXNeg || doXPos))) {
                            null
                        } else openEndSprite
                    )
                    .southSprite(
                        if (ConnectionUtils.isInternal(conn, Direction.SOUTH)) {
                            sideSprite
                        } else if (ConnectionUtils.isCorner(conn, Direction.SOUTH) || (ConnectionUtils.isDisconnected(
                                conn, Direction.SOUTH
                            ) && (doXNeg || doXPos))) {
                            null
                        } else openEndSprite
                    )
                    .northDecal1(
                        if (ConnectionUtils.isExternal(conn, Direction.NORTH) || (ConnectionUtils.isDisconnected(
                                conn, Direction.NORTH
                            ) && !(doXNeg || doXPos))) {
                            openEndDecal
                        } else null
                    )
                    .southDecal1(
                        if (ConnectionUtils.isExternal(conn, Direction.SOUTH) || (ConnectionUtils.isDisconnected(
                                conn, Direction.SOUTH
                            ) && !(doXNeg || doXPos))) {
                            openEndDecal
                        } else null
                    )
                    .material(material)
                    .decal1Material(decalMaterial)
                    .downCullFace(Direction.DOWN)
                    .setSideTexCoordsV(sideV)
                    .emit(emitter)
            }

            // emit corner boxes

            emitNorthWireCorner(
                conn, wireHeight, halfZWireWidth, sideSprite, topZSprite, openEndSprite, closedEndSprite, openEndDecal,
                sideV, material, decalMaterial, emitter
            )
            emitSouthWireCorner(
                conn, wireHeight, halfZWireWidth, sideSprite, topZSprite, openEndSprite, closedEndSprite, openEndDecal,
                sideV, material, decalMaterial, emitter
            )
            emitWestWireCorner(
                conn, wireHeight, halfXWireWidth, sideSprite, topXSprite, openEndSprite, closedEndSprite, openEndDecal,
                sideV, material, decalMaterial, emitter
            )
            emitEastWireCorner(
                conn, wireHeight, halfXWireWidth, sideSprite, topXSprite, openEndSprite, closedEndSprite, openEndDecal,
                sideV, material, decalMaterial, emitter
            )
        }
    }

    fun emitNorthWireCorner(
        conn: UByte, sideAxis: Direction.Axis, rotationAxis: Direction.Axis, wireHeight: Float, wireWidth: Float, sprite: Sprite,
        sideV: Float, material: RenderMaterial, emitter: QuadEmitter
    ) {
        emitNorthWireCorner(
            conn, wireHeight,
            if (rotationAxis == Direction.Axis.Z) halfZWireWidth(sideAxis, wireWidth) else halfXWireWidth(sideAxis, wireWidth),
            sprite, sprite, sprite, sprite, null, sideV, material, null, emitter
        )
    }

    fun emitSouthWireCorner(
        conn: UByte, sideAxis: Direction.Axis, rotationAxis: Direction.Axis, wireHeight: Float, wireWidth: Float, sprite: Sprite,
        sideV: Float, material: RenderMaterial, emitter: QuadEmitter
    ) {
        emitSouthWireCorner(
            conn, wireHeight,
            if (rotationAxis == Direction.Axis.Z) halfZWireWidth(sideAxis, wireWidth) else halfXWireWidth(sideAxis, wireWidth),
            sprite, sprite, sprite, sprite, null, sideV, material, null, emitter
        )
    }

    fun emitWestWireCorner(
        conn: UByte, sideAxis: Direction.Axis, rotationAxis: Direction.Axis, wireHeight: Float, wireWidth: Float, sprite: Sprite,
        sideV: Float, material: RenderMaterial, emitter: QuadEmitter
    ) {
        emitWestWireCorner(
            conn, wireHeight,
            if (rotationAxis == Direction.Axis.Z) halfXWireWidth(sideAxis, wireWidth) else halfZWireWidth(sideAxis, wireWidth),
            sprite, sprite, sprite, sprite, null, sideV, material, null, emitter
        )
    }

    fun emitEastWireCorner(
        conn: UByte, sideAxis: Direction.Axis, rotationAxis: Direction.Axis, wireHeight: Float, wireWidth: Float, sprite: Sprite,
        sideV: Float, material: RenderMaterial, emitter: QuadEmitter
    ) {
        emitEastWireCorner(
            conn, wireHeight,
            if (rotationAxis == Direction.Axis.Z) halfXWireWidth(sideAxis, wireWidth) else halfZWireWidth(sideAxis, wireWidth),
            sprite, sprite, sprite, sprite, null, sideV, material, null, emitter
        )
    }

    private fun emitNorthWireCorner(
        conn: UByte, wireHeight: Float, halfZWireWidth: Float, sideSprite: Sprite, topZSprite: Sprite,
        openEndSprite: Sprite, closedEndSprite: Sprite, openEndDecal: Sprite?, sideV: Float, material: RenderMaterial,
        decalMaterial: RenderMaterial?, emitter: QuadEmitter
    ) {
        if (ConnectionUtils.isCorner(conn, Direction.NORTH)) {
            BoxEmitter.onGroundPixels(
                8f - halfZWireWidth, -wireHeight + WIRE_CLEARANCE, 8f + halfZWireWidth, 0f, wireHeight
            )
                .sprite(sideSprite)
                .upSprite(topZSprite)
                .northSprite(closedEndSprite)
                .downSprite(openEndSprite)
                .downDecal1(openEndDecal)
                .southSprite(null)
                .material(material)
                .decal1Material(decalMaterial)
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
        openEndSprite: Sprite, closedEndSprite: Sprite, openEndDecal: Sprite?, sideV: Float, material: RenderMaterial,
        decalMaterial: RenderMaterial?, emitter: QuadEmitter
    ) {
        if (ConnectionUtils.isCorner(conn, Direction.SOUTH)) {
            BoxEmitter.onGroundPixels(
                8f - halfZWireWidth, 16f, 8f + halfZWireWidth, 16f + wireHeight - WIRE_CLEARANCE, wireHeight
            )
                .sprite(sideSprite)
                .upSprite(topZSprite)
                .southSprite(closedEndSprite)
                .downSprite(openEndSprite)
                .downDecal1(openEndDecal)
                .northSprite(null)
                .material(material)
                .decal1Material(decalMaterial)
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
        openEndSprite: Sprite, closedEndSprite: Sprite, openEndDecal: Sprite?, sideV: Float, material: RenderMaterial,
        decalMaterial: RenderMaterial?, emitter: QuadEmitter
    ) {
        if (ConnectionUtils.isCorner(conn, Direction.WEST)) {
            BoxEmitter.onGroundPixels(
                -wireHeight + WIRE_CLEARANCE, 8f - halfXWireWidth, 0f, 8f + halfXWireWidth, wireHeight
            )
                .sprite(sideSprite)
                .upSprite(topXSprite)
                .westSprite(closedEndSprite)
                .downSprite(openEndSprite)
                .downDecal1(openEndDecal)
                .eastSprite(null)
                .material(material)
                .decal1Material(decalMaterial)
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
        openEndSprite: Sprite, closedEndSprite: Sprite, openEndDecal: Sprite?, sideV: Float, material: RenderMaterial,
        decalMaterial: RenderMaterial?, emitter: QuadEmitter
    ) {
        if (ConnectionUtils.isCorner(conn, Direction.EAST)) {
            BoxEmitter.onGroundPixels(
                16f, 8f - halfXWireWidth, 16f + wireHeight - WIRE_CLEARANCE, 8f + halfXWireWidth, wireHeight
            )
                .sprite(sideSprite)
                .upSprite(topXSprite)
                .eastSprite(closedEndSprite)
                .downSprite(openEndSprite)
                .downDecal1(openEndDecal)
                .westSprite(null)
                .material(material)
                .decal1Material(decalMaterial)
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
        return if (!ConnectionUtils.isDisconnected(conn, cardinal)) {
            Pair(true, externalEnd)
        } else {
            Pair(false, 8f + if (cardinal == Direction.NORTH || cardinal == Direction.WEST) -wireWidth / 2f else wireWidth / 2f)
        }
    }

    private fun halfZWireWidth(axis: Direction.Axis, wireWidth: Float) =
        if (axis == Direction.Axis.Y) {
            wireWidth / 2f
        } else {
            wireWidth / 2f - WIRE_CLEARANCE
        }

    private fun halfXWireWidth(axis: Direction.Axis, wireWidth: Float) =
        if (axis != Direction.Axis.X) {
            wireWidth / 2f
        } else {
            wireWidth / 2f - WIRE_CLEARANCE
        }
}