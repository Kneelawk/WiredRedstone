package com.kneelawk.wiredredstone.client.render

import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter
import net.minecraft.client.texture.Sprite
import net.minecraft.util.math.Direction
import kotlin.math.max
import kotlin.math.min

class BoxEmitter(val minX: Float, val minY: Float, val minZ: Float, val maxX: Float, val maxY: Float, val maxZ: Float) {
    companion object {
        fun onGroundPixels(x1: Float, z1: Float, x2: Float, z2: Float, height: Float): BoxEmitter {
            return BoxEmitter(
                min(x1, x2) / 16f, 0f, min(z1, z2) / 16f, max(x1, x2) / 16f, height / 16f, max(z1, z2) / 16f
            )
        }
    }

    private sealed class TexCoords {
        abstract fun spriteBake(emitter: QuadEmitter, sprite: Sprite)

        object LockUV : TexCoords() {
            override fun spriteBake(emitter: QuadEmitter, sprite: Sprite) {
                emitter.spriteBake(0, sprite, QuadEmitter.BAKE_LOCK_UV)
            }
        }

        class CustomUV(val minU: Float, val minV: Float, val maxU: Float, val maxV: Float) : TexCoords() {
            override fun spriteBake(emitter: QuadEmitter, sprite: Sprite) {
                emitter.sprite(0, 0, minU, minV)
                emitter.sprite(1, 0, minU, maxV)
                emitter.sprite(2, 0, maxU, maxV)
                emitter.sprite(3, 0, maxU, minV)
                emitter.spriteBake(0, sprite, QuadEmitter.BAKE_NORMALIZED)
            }
        }
    }

    // If I need sided materials, I can implement that later.
    private var material: RenderMaterial? = null

    // If I need sided sprites, I can implement that later.
    private var downSprite: Sprite? = null
    private var upSprite: Sprite? = null
    private var northSprite: Sprite? = null
    private var southSprite: Sprite? = null
    private var westSprite: Sprite? = null
    private var eastSprite: Sprite? = null

    private var downTexCoords: TexCoords = TexCoords.LockUV
    private var upTexCoords: TexCoords = TexCoords.LockUV
    private var northTexCoords: TexCoords = TexCoords.LockUV
    private var southTexCoords: TexCoords = TexCoords.LockUV
    private var westTexCoords: TexCoords = TexCoords.LockUV
    private var eastTexCoords: TexCoords = TexCoords.LockUV

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

    fun sideTexCoordsCustomV(v: Float): BoxEmitter {
        val v2 = v + maxY - minY

        northTexCoords = TexCoords.CustomUV(1f - maxX, v, 1f - minX, v2)
        southTexCoords = TexCoords.CustomUV(minX, v, maxX, v2)
        westTexCoords = TexCoords.CustomUV(minZ, v, maxZ, v2)
        eastTexCoords = TexCoords.CustomUV(1f - maxZ, v, 1f - minZ, v2)

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

    fun emit(emitter: QuadEmitter) {
        downSprite?.let {
            // down
            emitter.nominalFace(Direction.DOWN)
            emitter.pos(0, minX, minY, maxZ)
            emitter.pos(1, minX, minY, minZ)
            emitter.pos(2, maxX, minY, minZ)
            emitter.pos(3, maxX, minY, maxZ)
            finishFace(emitter, downTexCoords, downCullFace, it)
        }

        downSprite?.let {
            // up
            emitter.nominalFace(Direction.UP)
            emitter.pos(0, minX, maxY, minZ)
            emitter.pos(1, minX, maxY, maxZ)
            emitter.pos(2, maxX, maxY, maxZ)
            emitter.pos(3, maxX, maxY, minZ)
            finishFace(emitter, upTexCoords, upCullFace, it)
        }

        downSprite?.let {
            // north
            emitter.nominalFace(Direction.NORTH)
            emitter.pos(0, maxX, maxY, minZ)
            emitter.pos(1, maxX, minY, minZ)
            emitter.pos(2, minX, minY, minZ)
            emitter.pos(3, minX, maxY, minZ)
            finishFace(emitter, northTexCoords, northCullFace, it)
        }

        downSprite?.let {
            // south
            emitter.nominalFace(Direction.SOUTH)
            emitter.pos(0, minX, maxY, maxZ)
            emitter.pos(1, minX, minY, maxZ)
            emitter.pos(2, maxX, minY, maxZ)
            emitter.pos(3, maxX, maxY, maxZ)
            finishFace(emitter, southTexCoords, southCullFace, it)
        }

        downSprite?.let {
            // west
            emitter.nominalFace(Direction.WEST)
            emitter.pos(0, minX, maxY, minZ)
            emitter.pos(1, minX, minY, minZ)
            emitter.pos(2, minX, minY, maxZ)
            emitter.pos(3, minX, maxY, maxZ)
            finishFace(emitter, westTexCoords, westCullFace, it)
        }

        downSprite?.let {
            // east
            emitter.nominalFace(Direction.EAST)
            emitter.pos(0, maxX, maxY, maxZ)
            emitter.pos(1, maxX, minY, maxZ)
            emitter.pos(2, maxX, minY, minZ)
            emitter.pos(3, maxX, maxY, minZ)
            finishFace(emitter, eastTexCoords, eastCullFace, it)
        }
    }

    private fun finishFace(emitter: QuadEmitter, texCoords: TexCoords, cullFace: Direction?, sprite: Sprite) {
        texCoords.spriteBake(emitter, sprite)
        emitter.spriteColor(0, -1, -1, -1, -1)
        material?.let { emitter.material(it) }
        emitter.cullFace(cullFace)
        emitter.emit()
    }
}