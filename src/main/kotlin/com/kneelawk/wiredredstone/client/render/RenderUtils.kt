package com.kneelawk.wiredredstone.client.render

import com.kneelawk.wiredredstone.util.ConnectionUtils.isCorner
import com.kneelawk.wiredredstone.util.ConnectionUtils.isExternal
import com.kneelawk.wiredredstone.util.ConnectionUtils.isInternal
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter
import net.minecraft.client.MinecraftClient
import net.minecraft.client.texture.Sprite
import net.minecraft.client.texture.SpriteAtlasTexture
import net.minecraft.util.Identifier
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Direction.*

object RenderUtils {
    fun getBlockSprite(id: Identifier): Sprite {
        return MinecraftClient.getInstance().getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE).apply(id)
    }

    fun emitWire(
        connections: UByte, axis: Axis, wireHeight: Float, wireWidth: Float, baseSprite: Sprite, endSprite: Sprite,
        sideV: Float, material: RenderMaterial, emitter: QuadEmitter
    ) {
        if (connections == 0u.toUByte()) {
            // no connections
            BoxEmitter.onGroundPixels(
                8f - wireWidth, 8f - wireWidth / 2f, 8f + wireWidth, 8f + wireWidth / 2f, wireHeight
            )
                .sprite(baseSprite)
                .westSprite(endSprite)
                .eastSprite(endSprite)
                .material(material)
                .downCullFace(DOWN)
                .sideTexCoordsCustomV(sideV)
                .emit(emitter)
        } else {
            val (doZNeg, zNegEnd) = calculateConnection(
                connections, axis, wireWidth, NORTH, wireHeight, 0f, -wireHeight, Axis.Y, true
            )
            val (doXNeg, xNegEnd) = calculateConnection(
                connections, axis, wireWidth, WEST, wireHeight, 0f, -wireHeight, Axis.X, false
            )
            val (doZPos, zPosEnd) = calculateConnection(
                connections, axis, wireWidth, SOUTH, 16f - wireHeight, 16f, 16f + wireHeight, Axis.Y, true
            )
            val (doXPos, xPosEnd) = calculateConnection(
                connections, axis, wireWidth, EAST, 16f - wireHeight, 16f, 16f + wireHeight, Axis.X, false
            )

            if (doXNeg || doXPos) {
                BoxEmitter.onGroundPixels(xNegEnd, 8f - wireWidth / 2f, xPosEnd, 8f + wireWidth / 2f, wireHeight)
                    .sprite(baseSprite)
                    .westSprite(if (isExternal(connections, WEST)) endSprite else baseSprite)
                    .eastSprite(if (isExternal(connections, EAST)) endSprite else baseSprite)
                    .material(material)
                    .downCullFace(DOWN)
                    .sideTexCoordsCustomV(sideV)
                    .emit(emitter)
            }

            if (doZNeg || doZPos) {
                BoxEmitter.onGroundPixels(8f - wireWidth / 2f, zNegEnd, 8f + wireWidth / 2f, zPosEnd, wireHeight)
                    .sprite(baseSprite)
                    .northSprite(if (isExternal(connections, NORTH)) endSprite else baseSprite)
                    .southSprite(if (isExternal(connections, SOUTH)) endSprite else baseSprite)
                    .material(material)
                    .downCullFace(DOWN)
                    .sideTexCoordsCustomV(sideV)
                    .emit(emitter)
            }
        }
    }

    private fun calculateConnection(
        connections: UByte, axis: Axis, wireWidth: Float, cardinal: Direction, internalEnd: Float, externalEnd: Float,
        cornerEnd: Float, specialAxis: Axis, axisIsLarger: Boolean
    ): Pair<Boolean, Float> {
        return if (isInternal(connections, cardinal)) {
            Pair(true, if ((axis == specialAxis) == axisIsLarger) externalEnd else internalEnd)
        } else if (isExternal(connections, cardinal)) {
            Pair(true, externalEnd)
        } else if (isCorner(connections, cardinal)) {
            Pair(true, if ((axis == specialAxis) == axisIsLarger) cornerEnd else externalEnd)
        } else {
            Pair(false, 8f + if (cardinal == NORTH || cardinal == WEST) -wireWidth / 2f else wireWidth / 2f)
        }
    }
}