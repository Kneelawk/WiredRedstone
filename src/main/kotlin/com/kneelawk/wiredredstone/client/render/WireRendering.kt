package com.kneelawk.wiredredstone.client.render

import com.kneelawk.wiredredstone.util.bits.CenterConnectionUtils
import com.kneelawk.wiredredstone.util.bits.ConnectionUtils.isCorner
import com.kneelawk.wiredredstone.util.bits.ConnectionUtils.isDisconnected
import com.kneelawk.wiredredstone.util.bits.ConnectionUtils.isExternal
import com.kneelawk.wiredredstone.util.bits.ConnectionUtils.isInternal
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter
import net.minecraft.client.texture.Sprite
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Direction.*

object WireRendering {
    private const val WIRE_CLEARANCE = 0.0005f

    fun emitWire(
        conn: UByte, side: Direction, wireHeight: Float, wireWidth: Float, topCrossSprite: Sprite,
        topXSprite: Sprite = topCrossSprite, topZSprite: Sprite = topCrossSprite,
        bottomCrossSprite: Sprite = topCrossSprite, bottomXSprite: Sprite = topXSprite,
        bottomZSprite: Sprite = topZSprite, sideSprite: Sprite = topCrossSprite, upperSideSprite: Sprite = sideSprite,
        cornerTopXSprite: Sprite = topXSprite, cornerTopZSprite: Sprite = topZSprite,
        cornerSideSprite: Sprite = sideSprite, cornerUpperSideSprite: Sprite = upperSideSprite,
        openEndSprite: Sprite = topZSprite, closedEndSprite: Sprite = topZSprite, openEndDecal: Sprite? = null,
        sideV: Float, material: RenderMaterial, decalMaterial: RenderMaterial? = null, emitter: QuadEmitter
    ) {
        if (conn == 0u.toUByte()) {
            // no connections
            BoxEmitter.onGround(
                0.5f - wireWidth, 0.5f - wireWidth / 2f, 0.5f + wireWidth, 0.5f + wireWidth / 2f, wireHeight
            )
                .downSprite(bottomXSprite)
                .upSprite(topXSprite)
                .northSprite(sideSprite)
                .southSprite(upperSideSprite)
                .westSprite(openEndSprite)
                .eastSprite(openEndSprite)
                .westDecal1(openEndDecal)
                .eastDecal1(openEndDecal)
                .material(material)
                .downCullFace(DOWN)
                .setSideTexCoordsV(sideV)
                .eastFlipU()
                .emit(emitter)
        } else {
            val axis = side.axis
            val invertX = side == WEST || side == SOUTH
            val invertZ = side == UP

            val (doZNeg, zNegEnd) = calculateConnection(conn, wireWidth, NORTH, 0f)
            val (doXNeg, xNegEnd) = calculateConnection(conn, wireWidth, WEST, 0f)
            val (doZPos, zPosEnd) = calculateConnection(conn, wireWidth, SOUTH, 1f)
            val (doXPos, xPosEnd) = calculateConnection(conn, wireWidth, EAST, 1f)

            // We want wires along some axis to be slightly thinner so that they don't overlap
            val halfXWireWidth = halfXWireWidth(axis, wireWidth)
            val halfZWireWidth = halfZWireWidth(axis, wireWidth)

            // Emit wire boxes

            if (doXNeg || doXPos) {
                BoxEmitter.onGround(xNegEnd, 0.5f - halfXWireWidth, xPosEnd, 0.5f + halfXWireWidth, wireHeight)
                    .downSprite(if (doZNeg || doZPos) bottomCrossSprite else bottomXSprite)
                    .upSprite(if (doZNeg || doZPos) topCrossSprite else topXSprite)
                    .northSprite(if (invertZ) upperSideSprite else sideSprite)
                    .southSprite(if (invertZ) sideSprite else upperSideSprite)
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
                    .westDecal1(
                        if (isExternal(conn, WEST) || (isDisconnected(conn, WEST) && !(doZNeg || doZPos))) {
                            openEndDecal
                        } else null
                    )
                    .eastDecal1(
                        if (isExternal(conn, EAST) || (isDisconnected(conn, EAST) && !(doZNeg || doZPos))) {
                            openEndDecal
                        } else null
                    )
                    .material(material)
                    .decal1Material(decalMaterial)
                    .downCullFace(DOWN)
                    .setSideTexCoordsV(sideV)
                    .apply {
                        if (invertX) upFlipU().downFlipU().northFlipU().southFlipU()
                        if (invertZ) upFlipV().westFlipU() else downFlipV().eastFlipU()
                    }
                    .emit(emitter)
            }

            if (doZNeg || doZPos) {
                BoxEmitter.onGround(0.5f - halfZWireWidth, zNegEnd, 0.5f + halfZWireWidth, zPosEnd, wireHeight)
                    .downSprite(if (doXNeg || doXPos) bottomCrossSprite else bottomZSprite)
                    .upSprite(if (doXNeg || doXPos) topCrossSprite else topZSprite)
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
                    .northDecal1(
                        if (isExternal(conn, NORTH) || (isDisconnected(conn, NORTH) && !(doXNeg || doXPos))) {
                            openEndDecal
                        } else null
                    )
                    .southDecal1(
                        if (isExternal(conn, SOUTH) || (isDisconnected(conn, SOUTH) && !(doXNeg || doXPos))) {
                            openEndDecal
                        } else null
                    )
                    .westSprite(if (invertX) upperSideSprite else sideSprite)
                    .eastSprite(if (invertX) sideSprite else upperSideSprite)
                    .material(material)
                    .decal1Material(decalMaterial)
                    .downCullFace(DOWN)
                    .setSideTexCoordsV(sideV)
                    .apply {
                        if (invertX) upFlipU().downFlipU().southFlipU() else northFlipU()
                        if (invertZ) upFlipV().westFlipU().eastFlipU() else downFlipV()
                    }
                    .emit(emitter)
            }

            // emit corner boxes

            emitNorthWireCorner(
                conn, side, wireHeight, halfZWireWidth, cornerSideSprite, cornerUpperSideSprite, cornerTopZSprite,
                openEndSprite, closedEndSprite, openEndDecal, sideV, material, decalMaterial, emitter
            )
            emitSouthWireCorner(
                conn, side, wireHeight, halfZWireWidth, cornerSideSprite, cornerUpperSideSprite, cornerTopZSprite,
                openEndSprite, closedEndSprite, openEndDecal, sideV, material, decalMaterial, emitter
            )
            emitWestWireCorner(
                conn, side, wireHeight, halfXWireWidth, cornerSideSprite, cornerUpperSideSprite, cornerTopXSprite,
                openEndSprite, closedEndSprite, openEndDecal, sideV, material, decalMaterial, emitter
            )
            emitEastWireCorner(
                conn, side, wireHeight, halfXWireWidth, cornerSideSprite, cornerUpperSideSprite, cornerTopXSprite,
                openEndSprite, closedEndSprite, openEndDecal, sideV, material, decalMaterial, emitter
            )
        }
    }

    fun emitNorthWireCorner(
        conn: UByte, side: Direction, rotationAxis: Axis, wireHeight: Float, wireWidth: Float,
        sprite: Sprite, sideV: Float, material: RenderMaterial, emitter: QuadEmitter
    ) {
        emitNorthWireCorner(
            conn, side, wireHeight,
            if (rotationAxis == Axis.Z) halfZWireWidth(side.axis, wireWidth) else halfXWireWidth(
                side.axis, wireWidth
            ),
            sprite, sprite, sprite, sprite, sprite, null, sideV, material, null, emitter
        )
    }

    fun emitSouthWireCorner(
        conn: UByte, side: Direction, rotationAxis: Axis, wireHeight: Float, wireWidth: Float,
        sprite: Sprite, sideV: Float, material: RenderMaterial, emitter: QuadEmitter
    ) {
        emitSouthWireCorner(
            conn, side, wireHeight,
            if (rotationAxis == Axis.Z) halfZWireWidth(side.axis, wireWidth) else halfXWireWidth(
                side.axis, wireWidth
            ),
            sprite, sprite, sprite, sprite, sprite, null, sideV, material, null, emitter
        )
    }

    fun emitWestWireCorner(
        conn: UByte, side: Direction, rotationAxis: Axis, wireHeight: Float, wireWidth: Float,
        sprite: Sprite, sideV: Float, material: RenderMaterial, emitter: QuadEmitter
    ) {
        emitWestWireCorner(
            conn, side, wireHeight,
            if (rotationAxis == Axis.Z) halfXWireWidth(side.axis, wireWidth) else halfZWireWidth(
                side.axis, wireWidth
            ),
            sprite, sprite, sprite, sprite, sprite, null, sideV, material, null, emitter
        )
    }

    fun emitEastWireCorner(
        conn: UByte, side: Direction, rotationAxis: Axis, wireHeight: Float, wireWidth: Float,
        sprite: Sprite, sideV: Float, material: RenderMaterial, emitter: QuadEmitter
    ) {
        emitEastWireCorner(
            conn, side, wireHeight,
            if (rotationAxis == Axis.Z) halfXWireWidth(side.axis, wireWidth) else halfZWireWidth(
                side.axis, wireWidth
            ),
            sprite, sprite, sprite, sprite, sprite, null, sideV, material, null, emitter
        )
    }

    private fun emitNorthWireCorner(
        conn: UByte, side: Direction, wireHeight: Float, halfZWireWidth: Float, sideSprite: Sprite,
        upperSideSprite: Sprite, topZSprite: Sprite, openEndSprite: Sprite, closedEndSprite: Sprite,
        openEndDecal: Sprite?, sideV: Float, material: RenderMaterial, decalMaterial: RenderMaterial?,
        emitter: QuadEmitter
    ) {
        if (isCorner(conn, NORTH)) {
            BoxEmitter.onGround(
                0.5f - halfZWireWidth, -wireHeight + WIRE_CLEARANCE, 0.5f + halfZWireWidth, 0f, wireHeight
            )
                .upSprite(topZSprite)
                .northSprite(closedEndSprite)
                .downSprite(openEndSprite)
                .downDecal1(openEndDecal)
                .southSprite(null)
                .westSprite(if (side == WEST || side == SOUTH) upperSideSprite else sideSprite)
                .eastSprite(if (side == WEST || side == SOUTH) sideSprite else upperSideSprite)
                .material(material)
                .decal1Material(decalMaterial)
                .setSideTexCoordsV(sideV)
                .translateDownTexCoords(0f, -sideV - wireHeight)
                .translateUpTexCoords(0f, 1f)
                .translateWestTexCoords(1f, 0f)
                .translateEastTexCoords(-1f, 0f)
                .apply {
                    if (side == DOWN || side == UP || side == EAST || side == NORTH) northFlipU()
                    if (side == WEST || side == SOUTH) upFlipU().downFlipU()
                    if (side == UP) upFlipV()
                }
                .downFlipV()
                .emit(emitter)
        }
    }

    private fun emitSouthWireCorner(
        conn: UByte, side: Direction, wireHeight: Float, halfZWireWidth: Float, sideSprite: Sprite,
        upperSideSprite: Sprite, topZSprite: Sprite, openEndSprite: Sprite, closedEndSprite: Sprite,
        openEndDecal: Sprite?, sideV: Float, material: RenderMaterial, decalMaterial: RenderMaterial?,
        emitter: QuadEmitter
    ) {
        if (isCorner(conn, SOUTH)) {
            BoxEmitter.onGround(
                0.5f - halfZWireWidth, 1f, 0.5f + halfZWireWidth, 1f + wireHeight - WIRE_CLEARANCE, wireHeight
            )
                .upSprite(topZSprite)
                .southSprite(closedEndSprite)
                .downSprite(openEndSprite)
                .downDecal1(openEndDecal)
                .northSprite(null)
                .westSprite(if (side == WEST || side == SOUTH) upperSideSprite else sideSprite)
                .eastSprite(if (side == WEST || side == SOUTH) sideSprite else upperSideSprite)
                .material(material)
                .decal1Material(decalMaterial)
                .setSideTexCoordsV(sideV)
                .translateDownTexCoords(0f, sideV + wireHeight)
                .translateUpTexCoords(0f, -1f)
                .translateWestTexCoords(-1f, 0f)
                .translateEastTexCoords(1f, 0f)
                .apply {
                    if (side == WEST || side == SOUTH) upFlipU().downFlipU().southFlipU()
                    if (side == UP) upFlipV()
                }
                .emit(emitter)
        }
    }

    private fun emitWestWireCorner(
        conn: UByte, side: Direction, wireHeight: Float, halfXWireWidth: Float, sideSprite: Sprite,
        upperSideSprite: Sprite, topXSprite: Sprite, openEndSprite: Sprite, closedEndSprite: Sprite,
        openEndDecal: Sprite?, sideV: Float, material: RenderMaterial, decalMaterial: RenderMaterial?,
        emitter: QuadEmitter
    ) {
        if (isCorner(conn, WEST)) {
            BoxEmitter.onGround(
                -wireHeight + WIRE_CLEARANCE, 0.5f - halfXWireWidth, 0f, 0.5f + halfXWireWidth, wireHeight
            )
                .upSprite(topXSprite)
                .westSprite(closedEndSprite)
                .downSprite(openEndSprite)
                .downDecal1(openEndDecal)
                .eastSprite(null)
                .northSprite(if (side == UP) upperSideSprite else sideSprite)
                .southSprite(if (side == UP) sideSprite else upperSideSprite)
                .material(material)
                .decal1Material(decalMaterial)
                .setSideTexCoordsV(sideV)
                .downTexCoords(0.5f - halfXWireWidth, sideV)
                .translateUpTexCoords(1f, 0f)
                .translateNorthTexCoords(-1f, 0f)
                .translateSouthTexCoords(1f, 0f)
                .downRotation(BoxEmitter.Rotation.DEGREES_270)
                .apply {
                    if (side == SOUTH || side == WEST) upFlipU()
                    if (side == UP) upFlipV().downFlipU().westFlipU()
                }
                .emit(emitter)
        }
    }

    private fun emitEastWireCorner(
        conn: UByte, side: Direction, wireHeight: Float, halfXWireWidth: Float, sideSprite: Sprite,
        upperSideSprite: Sprite, topXSprite: Sprite, openEndSprite: Sprite, closedEndSprite: Sprite,
        openEndDecal: Sprite?, sideV: Float, material: RenderMaterial, decalMaterial: RenderMaterial?,
        emitter: QuadEmitter
    ) {
        if (isCorner(conn, EAST)) {
            BoxEmitter.onGround(
                1f, 0.5f - halfXWireWidth, 1f + wireHeight - WIRE_CLEARANCE, 0.5f + halfXWireWidth, wireHeight
            )
                .upSprite(topXSprite)
                .eastSprite(closedEndSprite)
                .downSprite(openEndSprite)
                .downDecal1(openEndDecal)
                .westSprite(null)
                .northSprite(if (side == UP) upperSideSprite else sideSprite)
                .southSprite(if (side == UP) sideSprite else upperSideSprite)
                .material(material)
                .decal1Material(decalMaterial)
                .setSideTexCoordsV(sideV)
                .downTexCoords(0.5f - halfXWireWidth, sideV)
                .translateUpTexCoords(-1f, 0f)
                .translateNorthTexCoords(1f, 0f)
                .translateSouthTexCoords(-1f, 0f)
                .downRotation(BoxEmitter.Rotation.DEGREES_90)
                .apply {
                    if (side != UP) eastFlipU()
                    if (side == SOUTH || side == WEST) upFlipU()
                    if (side == UP) upFlipV() else downFlipU()
                }
                .emit(emitter)
        }
    }

    private fun calculateConnection(
        conn: UByte, wireWidth: Float, cardinal: Direction, externalEnd: Float
    ): Pair<Boolean, Float> {
        return if (!isDisconnected(conn, cardinal)) {
            Pair(true, externalEnd)
        } else {
            Pair(false, 0.5f + if (cardinal == NORTH || cardinal == WEST) -wireWidth / 2f else wireWidth / 2f)
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

    fun emitCenterWire(
        conn: UByte, wireDiameter: Float, topCrossSprite: Sprite, topXSprite: Sprite = topCrossSprite,
        topZSprite: Sprite = topCrossSprite, bottomCrossSprite: Sprite = topCrossSprite,
        bottomXSprite: Sprite = topXSprite, bottomZSprite: Sprite = topZSprite,
        lowerCrossSprite: Sprite = topCrossSprite, lowerXSprite: Sprite = topXSprite, lowerZSprite: Sprite = topZSprite,
        upperCrossSprite: Sprite = topCrossSprite, upperXSprite: Sprite = topXSprite, upperZSprite: Sprite = topZSprite,
        endSprite: Sprite = topCrossSprite, endDecalSprite: Sprite? = null, material: RenderMaterial,
        decalMaterial: RenderMaterial? = null, emitter: QuadEmitter
    ) {
        if (conn == 0u.toUByte()) {
            BoxEmitter.of(
                0.5f - wireDiameter / 2f, 0.5f - wireDiameter / 2f, 0.5f - wireDiameter / 2f, 0.5f + wireDiameter / 2f,
                0.5f + wireDiameter / 2f, 0.5f + wireDiameter / 2f
            )
                .downSprite(bottomXSprite)
                .upSprite(topXSprite)
                .northSprite(lowerXSprite)
                .southSprite(upperXSprite)
                .westSprite(endSprite)
                .eastSprite(endSprite)
                .westDecal1(endDecalSprite)
                .eastDecal1(endDecalSprite)
                .eastFlipU()
                .downFlipV()
                .material(material)
                .decal1Material(decalMaterial)
                .emit(emitter)
        } else {
            val (doYNeg, yNegEnd) = calculateCenterConnection(conn, wireDiameter, DOWN, 0f)
            val (doZNeg, zNegEnd) = calculateCenterConnection(conn, wireDiameter, NORTH, 0f)
            val (doXNeg, xNegEnd) = calculateCenterConnection(conn, wireDiameter, WEST, 0f)
            val (doYPos, yPosEnd) = calculateCenterConnection(conn, wireDiameter, UP, 1f)
            val (doZPos, zPosEnd) = calculateCenterConnection(conn, wireDiameter, SOUTH, 1f)
            val (doXPos, xPosEnd) = calculateCenterConnection(conn, wireDiameter, EAST, 1f)

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
                    .northSprite(if (doXNeg || doXPos) topCrossSprite else topZSprite)
                    .southSprite(if (doXNeg || doXPos) bottomCrossSprite else bottomZSprite)
                    .westSprite(if (doZNeg || doZPos) lowerCrossSprite else lowerZSprite)
                    .eastSprite(if (doZNeg || doZPos) upperCrossSprite else upperZSprite)
                    .northFlipU()
                    .eastFlipU()
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
                    .downSprite(if (doXNeg || doXPos) bottomCrossSprite else bottomZSprite)
                    .upSprite(if (doXNeg || doXPos) topCrossSprite else topZSprite)
                    .northSprite(if (doZNeg || !others) endSprite else null)
                    .northDecal1(if (doZNeg || !others) endDecalSprite else null)
                    .southSprite(if (doZPos || !others) endSprite else null)
                    .southDecal1(if (doZPos || !others) endDecalSprite else null)
                    .westSprite(if (doYNeg || doYPos) lowerCrossSprite else lowerXSprite)
                    .eastSprite(if (doYNeg || doYPos) upperCrossSprite else upperXSprite)
                    .northFlipU()
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
                    .downSprite(if (doZNeg || doZPos) bottomCrossSprite else bottomXSprite)
                    .upSprite(if (doZNeg || doZPos) topCrossSprite else topXSprite)
                    .northSprite(if (doYNeg || doYPos) lowerCrossSprite else lowerXSprite)
                    .southSprite(if (doYNeg || doYPos) upperCrossSprite else upperXSprite)
                    .westSprite(if (doXNeg || !others) endSprite else null)
                    .westDecal1(if (doXNeg || !others) endDecalSprite else null)
                    .eastSprite(if (doXPos || !others) endSprite else null)
                    .eastDecal1(if (doXPos || !others) endDecalSprite else null)
                    .eastFlipU()
                    .downFlipV()
                    .material(material)
                    .decal1Material(decalMaterial)
                    .emit(emitter)
            }
        }
    }

    private fun calculateCenterConnection(
        conn: UByte, wireDiameter: Float, dir: Direction, externalEnd: Float
    ): Pair<Boolean, Float> {
        return if (CenterConnectionUtils.test(conn, dir)) {
            Pair(true, externalEnd)
        } else {
            Pair(
                false, 0.5f + if (dir == DOWN || dir == NORTH || dir == WEST) -wireDiameter / 2f else wireDiameter / 2f
            )
        }
    }
}
