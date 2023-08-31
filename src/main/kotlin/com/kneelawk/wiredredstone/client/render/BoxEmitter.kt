package com.kneelawk.wiredredstone.client.render

import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter
import net.minecraft.client.texture.Sprite
import net.minecraft.util.math.Direction
import kotlin.math.max
import kotlin.math.min

/**
 * This thing is an eldritch abomination.
 */
class BoxEmitter(var minX: Float, var minY: Float, var minZ: Float, var maxX: Float, var maxY: Float, var maxZ: Float) {
    companion object {
        fun onGround(x1: Float, z1: Float, x2: Float, z2: Float, height: Float): BoxEmitter {
            return BoxEmitter(
                min(x1, x2), 0f, min(z1, z2), max(x1, x2), height, max(z1, z2)
            )
        }

        fun of(x1: Float, y1: Float, z1: Float, x2: Float, y2: Float, z2: Float): BoxEmitter {
            return BoxEmitter(min(x1, x2), min(y1, y2), min(z1, z2), max(x1, x2), max(y1, y2), max(z1, z2))
        }
    }

    class TexCoords(var minU: Float, var minV: Float, var maxU: Float, var maxV: Float) {
        fun prepare(): FullTexCoords {
            return FullTexCoords(
                minU, minV,
                minU, maxV,
                maxU, maxV,
                maxU, minV
            )
        }
    }

    class FullTexCoords(
        val u0: Float, val v0: Float, val u1: Float, val v1: Float, val u2: Float, val v2: Float, val u3: Float,
        val v3: Float
    ) {
        fun spriteBake(emitter: QuadEmitter, sprite: Sprite) {
            emitter.sprite(0, 0, u0, v0)
            emitter.sprite(1, 0, u1, v1)
            emitter.sprite(2, 0, u2, v2)
            emitter.sprite(3, 0, u3, v3)
            emitter.spriteBake(0, sprite, QuadEmitter.BAKE_NORMALIZED)
        }
    }

    /**
     * This directly rotates a quad in place, making sure it keeps the correct aspect ratio. This is unlike [Flip] which flips the entire `u` and `v` coordinates.
     */
    enum class Rotation(val transform1: (TexCoords) -> TexCoords, val transform2: (FullTexCoords) -> FullTexCoords) {
        DEGREES_0({ it }, { it }),
        DEGREES_90({
            TexCoords(it.minU, it.minV, it.minU + it.maxV - it.minV, it.minV + it.maxU - it.minU)
        }, {
            FullTexCoords(
                it.u1, it.v1,
                it.u2, it.v2,
                it.u3, it.v3,
                it.u0, it.v0
            )
        }),
        DEGREES_180({ it }, {
            FullTexCoords(
                it.u2, it.v2,
                it.u3, it.v3,
                it.u0, it.v0,
                it.u1, it.v1
            )
        }),
        DEGREES_270({
            TexCoords(it.minU, it.minV, it.minU + it.maxV - it.minV, it.minV + it.maxU - it.minU)
        }, {
            FullTexCoords(
                it.u3, it.v3,
                it.u0, it.v0,
                it.u1, it.v1,
                it.u2, it.v2
            )
        });
    }

    /**
     * Flips the entire `u` and `v` coordinates along the `u`-axis and/or the `v`-axis. This is unlike [Rotation] which rotates quads in place.
     */
    enum class Flip(val transform: (FullTexCoords) -> FullTexCoords) {
        FLIP_NONE({ it }),
        FLIP_U({
            FullTexCoords(
                1f - it.u0, it.v0,
                1f - it.u1, it.v1,
                1f - it.u2, it.v2,
                1f - it.u3, it.v3
            )
        }),
        FLIP_V({
            FullTexCoords(
                it.u0, 1f - it.v0,
                it.u1, 1f - it.v1,
                it.u2, 1f - it.v2,
                it.u3, 1f - it.v3
            )
        }),
        FLIP_UV({
            FullTexCoords(
                1f - it.u0, 1f - it.v0,
                1f - it.u1, 1f - it.v1,
                1f - it.u2, 1f - it.v2,
                1f - it.u3, 1f - it.v3
            )
        });

        infix fun or(flip: Flip): Flip {
            if (this == flip) return this
            if (this == FLIP_NONE) return flip
            if (flip == FLIP_NONE) return this
            return FLIP_UV
        }
    }

    private var material: RenderMaterial? = null
    private var decal1Material: RenderMaterial? = null

    private var downSprite: Sprite? = null
    private var upSprite: Sprite? = null
    private var northSprite: Sprite? = null
    private var southSprite: Sprite? = null
    private var westSprite: Sprite? = null
    private var eastSprite: Sprite? = null

    private var downDecal1: Sprite? = null
    private var upDecal1: Sprite? = null
    private var northDecal1: Sprite? = null
    private var southDecal1: Sprite? = null
    private var westDecal1: Sprite? = null
    private var eastDecal1: Sprite? = null

    private var downTexCoords: TexCoords = TexCoords(minX, 1f - maxZ, maxX, 1f - minZ)
    private var upTexCoords: TexCoords = TexCoords(minX, minZ, maxX, maxZ)
    private var northTexCoords: TexCoords = TexCoords(1f - maxX, 1f - maxY, 1f - minX, 1f - minY)
    private var southTexCoords: TexCoords = TexCoords(minX, 1f - maxY, maxX, 1f - minY)
    private var westTexCoords: TexCoords = TexCoords(minZ, 1f - maxY, maxZ, 1f - minY)
    private var eastTexCoords: TexCoords = TexCoords(1f - maxZ, 1f - maxY, 1f - minZ, 1f - minY)

    private var downRotation: Rotation = Rotation.DEGREES_0
    private var upRotation: Rotation = Rotation.DEGREES_0
    private var northRotation: Rotation = Rotation.DEGREES_0
    private var southRotation: Rotation = Rotation.DEGREES_0
    private var westRotation: Rotation = Rotation.DEGREES_0
    private var eastRotation: Rotation = Rotation.DEGREES_0

    private var downFlip: Flip = Flip.FLIP_NONE
    private var upFlip: Flip = Flip.FLIP_NONE
    private var northFlip: Flip = Flip.FLIP_NONE
    private var southFlip: Flip = Flip.FLIP_NONE
    private var westFlip: Flip = Flip.FLIP_NONE
    private var eastFlip: Flip = Flip.FLIP_NONE

    private var downCullFace: Direction? = null
    private var upCullFace: Direction? = null
    private var northCullFace: Direction? = null
    private var southCullFace: Direction? = null
    private var westCullFace: Direction? = null
    private var eastCullFace: Direction? = null

    fun material(material: RenderMaterial?): BoxEmitter {
        this.material = material
        return this
    }

    fun decal1Material(material: RenderMaterial?): BoxEmitter {
        this.decal1Material = material
        return this
    }

    fun sprite(sprite: Sprite?): BoxEmitter {
        downSprite = sprite
        upSprite = sprite
        northSprite = sprite
        southSprite = sprite
        westSprite = sprite
        eastSprite = sprite
        return this
    }

    fun downSprite(sprite: Sprite?): BoxEmitter {
        downSprite = sprite
        return this
    }

    fun upSprite(sprite: Sprite?): BoxEmitter {
        upSprite = sprite
        return this
    }

    fun northSprite(sprite: Sprite?): BoxEmitter {
        northSprite = sprite
        return this
    }

    fun southSprite(sprite: Sprite?): BoxEmitter {
        southSprite = sprite
        return this
    }

    fun westSprite(sprite: Sprite?): BoxEmitter {
        westSprite = sprite
        return this
    }

    fun eastSprite(sprite: Sprite?): BoxEmitter {
        eastSprite = sprite
        return this
    }

    fun decal1(sprite: Sprite?): BoxEmitter {
        downDecal1 = sprite
        upDecal1 = sprite
        northDecal1 = sprite
        southDecal1 = sprite
        westDecal1 = sprite
        eastDecal1 = sprite
        return this
    }

    fun downDecal1(sprite: Sprite?): BoxEmitter {
        downDecal1 = sprite
        return this
    }

    fun upDecal1(sprite: Sprite?): BoxEmitter {
        upDecal1 = sprite
        return this
    }

    fun northDecal1(sprite: Sprite?): BoxEmitter {
        northDecal1 = sprite
        return this
    }

    fun southDecal1(sprite: Sprite?): BoxEmitter {
        southDecal1 = sprite
        return this
    }

    fun westDecal1(sprite: Sprite?): BoxEmitter {
        westDecal1 = sprite
        return this
    }

    fun eastDecal1(sprite: Sprite?): BoxEmitter {
        eastDecal1 = sprite
        return this
    }

    fun setSideTexCoordsV(v: Float): BoxEmitter {
        val v2 = v + maxY - minY

        northTexCoords.minV = v
        northTexCoords.maxV = v2
        southTexCoords.minV = v
        southTexCoords.maxV = v2
        westTexCoords.minV = v
        westTexCoords.maxV = v2
        eastTexCoords.minV = v
        eastTexCoords.maxV = v2

        return this
    }

    fun translateDownTexCoords(u: Float, v: Float): BoxEmitter {
        downTexCoords.minU += u
        downTexCoords.maxU += u
        downTexCoords.minV += v
        downTexCoords.maxV += v
        return this
    }

    fun translateUpTexCoords(u: Float, v: Float): BoxEmitter {
        upTexCoords.minU += u
        upTexCoords.maxU += u
        upTexCoords.minV += v
        upTexCoords.maxV += v
        return this
    }

    fun translateNorthTexCoords(u: Float, v: Float): BoxEmitter {
        northTexCoords.minU += u
        northTexCoords.maxU += u
        northTexCoords.minV += v
        northTexCoords.maxV += v
        return this
    }

    fun translateSouthTexCoords(u: Float, v: Float): BoxEmitter {
        southTexCoords.minU += u
        southTexCoords.maxU += u
        southTexCoords.minV += v
        southTexCoords.maxV += v
        return this
    }

    fun translateWestTexCoords(u: Float, v: Float): BoxEmitter {
        westTexCoords.minU += u
        westTexCoords.maxU += u
        westTexCoords.minV += v
        westTexCoords.maxV += v
        return this
    }

    fun translateEastTexCoords(u: Float, v: Float): BoxEmitter {
        eastTexCoords.minU += u
        eastTexCoords.maxU += u
        eastTexCoords.minV += v
        eastTexCoords.maxV += v
        return this
    }

    fun downTexCoords(u: Float, v: Float): BoxEmitter {
        setTexCoords(downTexCoords, u, v)
        return this
    }

    fun upTexCoords(u: Float, v: Float): BoxEmitter {
        setTexCoords(upTexCoords, u, v)
        return this
    }

    fun northTexCoords(u: Float, v: Float): BoxEmitter {
        setTexCoords(northTexCoords, u, v)
        return this
    }

    fun southTexCoords(u: Float, v: Float): BoxEmitter {
        setTexCoords(southTexCoords, u, v)
        return this
    }

    fun westTexCoords(u: Float, v: Float): BoxEmitter {
        setTexCoords(westTexCoords, u, v)
        return this
    }

    fun eastTexCoords(u: Float, v: Float): BoxEmitter {
        setTexCoords(eastTexCoords, u, v)
        return this
    }

    private fun setTexCoords(texCoords: TexCoords, u: Float, v: Float) {
        val u2 = u + texCoords.maxU - texCoords.minU
        val v2 = v + texCoords.maxV - texCoords.minV
        texCoords.minU = u
        texCoords.maxU = u2
        texCoords.minV = v
        texCoords.maxV = v2
    }

    fun downRotation(rotation: Rotation): BoxEmitter {
        downRotation = rotation
        return this
    }

    fun upRotation(rotation: Rotation): BoxEmitter {
        upRotation = rotation
        return this
    }

    fun northRotation(rotation: Rotation): BoxEmitter {
        northRotation = rotation
        return this
    }

    fun southRotation(rotation: Rotation): BoxEmitter {
        southRotation = rotation
        return this
    }

    fun westRotation(rotation: Rotation): BoxEmitter {
        westRotation = rotation
        return this
    }

    fun eastRotation(rotation: Rotation): BoxEmitter {
        eastRotation = rotation
        return this
    }

    fun downFlipV(): BoxEmitter {
        downFlip = downFlip or Flip.FLIP_V
        return this
    }

    fun upFlipV(): BoxEmitter {
        upFlip = upFlip or Flip.FLIP_V
        return this
    }

    fun northFlipV(): BoxEmitter {
        northFlip = northFlip or Flip.FLIP_V
        return this
    }

    fun southFlipV(): BoxEmitter {
        southFlip = southFlip or Flip.FLIP_V
        return this
    }

    fun westFlipV(): BoxEmitter {
        westFlip = westFlip or Flip.FLIP_V
        return this
    }

    fun eastFlipV(): BoxEmitter {
        eastFlip = eastFlip or Flip.FLIP_V
        return this
    }

    fun downFlipU(): BoxEmitter {
        downFlip = downFlip or Flip.FLIP_U
        return this
    }

    fun upFlipU(): BoxEmitter {
        upFlip = upFlip or Flip.FLIP_U
        return this
    }

    fun northFlipU(): BoxEmitter {
        northFlip = northFlip or Flip.FLIP_U
        return this
    }

    fun southFlipU(): BoxEmitter {
        southFlip = southFlip or Flip.FLIP_U
        return this
    }

    fun westFlipU(): BoxEmitter {
        westFlip = westFlip or Flip.FLIP_U
        return this
    }

    fun eastFlipU(): BoxEmitter {
        eastFlip = eastFlip or Flip.FLIP_U
        return this
    }

    fun downCullFace(cullFace: Direction?): BoxEmitter {
        downCullFace = cullFace
        return this
    }

    fun upCullFace(cullFace: Direction?): BoxEmitter {
        upCullFace = cullFace
        return this
    }

    fun northCullFace(cullFace: Direction?): BoxEmitter {
        northCullFace = cullFace
        return this
    }

    fun southCullFace(cullFace: Direction?): BoxEmitter {
        southCullFace = cullFace
        return this
    }

    fun westCullFace(cullFace: Direction?): BoxEmitter {
        westCullFace = cullFace
        return this
    }

    fun eastCullFace(cullFace: Direction?): BoxEmitter {
        eastCullFace = cullFace
        return this
    }

    fun widenX(amount: Float): BoxEmitter {
        minX -= amount
        maxX += amount
        return this
    }

    fun widenY(amount: Float): BoxEmitter {
        minY -= amount
        maxY += amount
        return this
    }

    fun widenZ(amount: Float): BoxEmitter {
        minZ -= amount
        maxZ += amount
        return this
    }

    fun extendDown(amount: Float): BoxEmitter {
        minY -= amount
        return this
    }

    fun extendUp(amount: Float): BoxEmitter {
        maxY += amount
        return this
    }

    fun extendNorth(amount: Float): BoxEmitter {
        minZ -= amount
        return this
    }

    fun extendSouth(amount: Float): BoxEmitter {
        maxZ += amount
        return this
    }

    fun extendWest(amount: Float): BoxEmitter {
        minX -= amount
        return this
    }

    fun extendEast(amount: Float): BoxEmitter {
        maxX += amount
        return this
    }

    fun emit(emitter: QuadEmitter) {
        emitBox(emitter, material, downSprite, upSprite, northSprite, southSprite, westSprite, eastSprite)
        emitBox(emitter, decal1Material, downDecal1, upDecal1, northDecal1, southDecal1, westDecal1, eastDecal1)
    }

    private fun emitBox(
        emitter: QuadEmitter, material: RenderMaterial?, downSprite: Sprite?, upSprite: Sprite?, northSprite: Sprite?,
        southSprite: Sprite?, westSprite: Sprite?, eastSprite: Sprite?
    ) {
        downSprite?.let {
            // down
            emitter.nominalFace(Direction.DOWN)
            emitter.pos(0, minX, minY, maxZ)
            emitter.pos(1, minX, minY, minZ)
            emitter.pos(2, maxX, minY, minZ)
            emitter.pos(3, maxX, minY, maxZ)
            emitter.normal(0, 0f, -1f, 0f)
            emitter.normal(1, 0f, -1f, 0f)
            emitter.normal(2, 0f, -1f, 0f)
            emitter.normal(3, 0f, -1f, 0f)
            finishFace(emitter, downTexCoords, material, downCullFace, it, downRotation, downFlip)
        }

        upSprite?.let {
            // up
            emitter.nominalFace(Direction.UP)
            emitter.pos(0, minX, maxY, minZ)
            emitter.pos(1, minX, maxY, maxZ)
            emitter.pos(2, maxX, maxY, maxZ)
            emitter.pos(3, maxX, maxY, minZ)
            emitter.normal(0, 0f, 1f, 0f)
            emitter.normal(1, 0f, 1f, 0f)
            emitter.normal(2, 0f, 1f, 0f)
            emitter.normal(3, 0f, 1f, 0f)
            finishFace(emitter, upTexCoords, material, upCullFace, it, upRotation, upFlip)
        }

        northSprite?.let {
            // north
            emitter.nominalFace(Direction.NORTH)
            emitter.pos(0, maxX, maxY, minZ)
            emitter.pos(1, maxX, minY, minZ)
            emitter.pos(2, minX, minY, minZ)
            emitter.pos(3, minX, maxY, minZ)
            emitter.normal(0, 0f, 0f, -1f)
            emitter.normal(1, 0f, 0f, -1f)
            emitter.normal(2, 0f, 0f, -1f)
            emitter.normal(3, 0f, 0f, -1f)
            finishFace(emitter, northTexCoords, material, northCullFace, it, northRotation, northFlip)
        }

        southSprite?.let {
            // south
            emitter.nominalFace(Direction.SOUTH)
            emitter.pos(0, minX, maxY, maxZ)
            emitter.pos(1, minX, minY, maxZ)
            emitter.pos(2, maxX, minY, maxZ)
            emitter.pos(3, maxX, maxY, maxZ)
            emitter.normal(0, 0f, 0f, 1f)
            emitter.normal(1, 0f, 0f, 1f)
            emitter.normal(2, 0f, 0f, 1f)
            emitter.normal(3, 0f, 0f, 1f)
            finishFace(emitter, southTexCoords, material, southCullFace, it, southRotation, southFlip)
        }

        westSprite?.let {
            // west
            emitter.nominalFace(Direction.WEST)
            emitter.pos(0, minX, maxY, minZ)
            emitter.pos(1, minX, minY, minZ)
            emitter.pos(2, minX, minY, maxZ)
            emitter.pos(3, minX, maxY, maxZ)
            emitter.normal(0, -1f, 0f, 0f)
            emitter.normal(1, -1f, 0f, 0f)
            emitter.normal(2, -1f, 0f, 0f)
            emitter.normal(3, -1f, 0f, 0f)
            finishFace(emitter, westTexCoords, material, westCullFace, it, westRotation, westFlip)
        }

        eastSprite?.let {
            // east
            emitter.nominalFace(Direction.EAST)
            emitter.pos(0, maxX, maxY, maxZ)
            emitter.pos(1, maxX, minY, maxZ)
            emitter.pos(2, maxX, minY, minZ)
            emitter.pos(3, maxX, maxY, minZ)
            emitter.normal(0, 1f, 0f, 0f)
            emitter.normal(1, 1f, 0f, 0f)
            emitter.normal(2, 1f, 0f, 0f)
            emitter.normal(3, 1f, 0f, 0f)
            finishFace(emitter, eastTexCoords, material, eastCullFace, it, eastRotation, eastFlip)
        }
    }

    private fun finishFace(
        emitter: QuadEmitter, texCoords: TexCoords, material: RenderMaterial?, cullFace: Direction?, sprite: Sprite,
        rotation: Rotation,
        flip: Flip
    ) {
        flip.transform(rotation.transform2(rotation.transform1(texCoords).prepare())).spriteBake(emitter, sprite)
        emitter.spriteColor(0, -1, -1, -1, -1)
        material?.let { emitter.material(it) }
        emitter.cullFace(cullFace)
        emitter.emit()
    }
}
