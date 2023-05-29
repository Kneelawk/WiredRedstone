package com.kneelawk.wiredredstone.client.render

import net.fabricmc.fabric.api.renderer.v1.RendererAccess
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadView
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext
import net.minecraft.client.render.VertexFormats
import net.minecraft.client.render.model.BakedQuad
import net.minecraft.client.texture.Sprite
import net.minecraft.util.math.Direction
import org.joml.Vector2f
import org.joml.Vector3f
import org.lwjgl.system.MemoryStack
import java.util.*

sealed class TransformingQuadEmitter(private val emitter: QuadEmitter) : QuadEmitter {
    companion object {
        private val DEFAULT_MATERIAL by lazy {
            RendererAccess.INSTANCE.renderer!!.materialFinder().find()
        }
    }

    class Single(emitter: QuadEmitter, private val transform: RenderContext.QuadTransform) :
        TransformingQuadEmitter(emitter) {
        override fun transform(quadView: MutableQuadView): Boolean {
            return transform.transform(quadView)
        }
    }

    class Multi(emitter: QuadEmitter, private val transforms: Array<RenderContext.QuadTransform>) :
        TransformingQuadEmitter(emitter) {
        override fun transform(quadView: MutableQuadView): Boolean {
            for (transform in transforms) {
                if (!transform.transform(quadView)) {
                    return false
                }
            }
            return true
        }
    }

    private var positions = Array(4) { Vector3f() }
    private var normals = Array(4) { Vector3f() }
    private var hasNormal = BooleanArray(4)
    private var material: RenderMaterial = DEFAULT_MATERIAL
    private var colorIndex: Int = -1
    private var cullFace: Direction? = null
    private var nominalFace: Direction? = null
    private var tag: Int = 0
    private var lightmaps = IntArray(4)
    private var hasLightmap = BooleanArray(4)
    private var spriteColors = IntArray(4).also { Arrays.fill(it, -1) }
    private var texCoordUs = FloatArray(4)
    private var texCoordVs = FloatArray(4)
    private var sprite: Sprite? = null
    private var spriteBakeFlags = 0

    private fun clear() {
        positions.forEach { it.set(0f, 0f, 0f) }
        normals.forEach { it.set(0f, 0f, 0f) }
        Arrays.fill(hasNormal, false)
        material = DEFAULT_MATERIAL
        colorIndex = -1
        cullFace = null
        nominalFace = null
        tag = 0
        Arrays.fill(lightmaps, 0)
        Arrays.fill(hasLightmap, false)
        Arrays.fill(spriteColors, -1)
        Arrays.fill(texCoordUs, 0f)
        Arrays.fill(texCoordVs, 0f)
        sprite = null
        spriteBakeFlags = 0
    }

    protected abstract fun transform(quadView: MutableQuadView): Boolean

    override fun toVanilla(spriteIndex: Int, target: IntArray?, targetIndex: Int, isItem: Boolean) {
        throw NotImplementedError("TransformingQuadEmitter.toVanilla")
    }

    override fun toVanilla(target: IntArray?, targetIndex: Int) {
        throw NotImplementedError("TransformingQuadEmitter.fromVanilla")
    }

    override fun copyTo(target: MutableQuadView) {
        for (vertexIndex in 0 until 4) {
            target.pos(vertexIndex, positions[vertexIndex])
            if (hasNormal[vertexIndex]) {
                target.normal(vertexIndex, normals[vertexIndex])
            }
            if (hasLightmap[vertexIndex]) {
                target.lightmap(vertexIndex, lightmaps[vertexIndex])
            }
            target.color(vertexIndex, spriteColors[vertexIndex])
            target.uv(vertexIndex, texCoordUs[vertexIndex], texCoordVs[vertexIndex])
        }

        target.material(material)
        target.colorIndex(colorIndex)
        target.cullFace(cullFace)
        if (nominalFace != null) {
            target.nominalFace(nominalFace)
        }
        target.tag(tag)
        if (sprite != null) {
            target.spriteBake(sprite, spriteBakeFlags)
        }
    }

    override fun copyFrom(quad: QuadView): QuadEmitter {
        for (vertexIndex in 0 until 4) {
            quad.copyPos(vertexIndex, positions[vertexIndex])
            hasNormal[vertexIndex] = quad.hasNormal(vertexIndex);
            if (hasNormal[vertexIndex]) {
                quad.copyNormal(vertexIndex, normals[vertexIndex])
            }
            hasLightmap[vertexIndex] = true
            lightmaps[vertexIndex] = quad.lightmap(vertexIndex)
            spriteColors[vertexIndex] = quad.color(vertexIndex)
            texCoordUs[vertexIndex] = quad.u(vertexIndex)
            texCoordVs[vertexIndex] = quad.v(vertexIndex)
        }

        material = quad.material()
        colorIndex = quad.colorIndex()
        cullFace = quad.cullFace()
        nominalFace = quad.nominalFace()
        tag = quad.tag()

        return this
    }

    override fun material(material: RenderMaterial?): QuadEmitter {
        this.material = material ?: DEFAULT_MATERIAL
        return this
    }

    override fun material(): RenderMaterial {
        return material
    }

    override fun colorIndex(colorIndex: Int): QuadEmitter {
        this.colorIndex = colorIndex
        return this
    }

    override fun colorIndex(): Int {
        return colorIndex
    }

    override fun lightFace(): Direction {
        throw NotImplementedError("TransformingQuadEmitter.lightFace")
    }

    override fun cullFace(face: Direction?): QuadEmitter {
        cullFace = face
        return this
    }

    override fun cullFace(): Direction? {
        return cullFace
    }

    override fun nominalFace(face: Direction?): QuadEmitter {
        nominalFace = face
        return this
    }

    override fun nominalFace(): Direction? {
        return nominalFace
    }

    override fun faceNormal(): Vector3f {
        throw NotImplementedError("TransformingQuadEmitter.faceNormal")
    }

    override fun tag(tag: Int): QuadEmitter {
        this.tag = tag
        return this
    }

    override fun tag(): Int {
        return tag
    }

    override fun copyPos(vertexIndex: Int, target: Vector3f?): Vector3f {
        val to = target ?: Vector3f()
        to.set(positions[vertexIndex])
        return to
    }

    override fun posByIndex(vertexIndex: Int, coordinateIndex: Int): Float {
        val from = positions[vertexIndex]
        return when (coordinateIndex) {
            0 -> from.x
            1 -> from.y
            2 -> from.z
            else -> throw IllegalArgumentException("Coordinate indices must be between 0 (inclusive) and 3 (exclusive)")
        }
    }

    override fun x(vertexIndex: Int): Float {
        return positions[vertexIndex].x
    }

    override fun y(vertexIndex: Int): Float {
        return positions[vertexIndex].y
    }

    override fun z(vertexIndex: Int): Float {
        return positions[vertexIndex].z
    }

    override fun hasNormal(vertexIndex: Int): Boolean {
        return hasNormal[vertexIndex]
    }

    override fun copyNormal(vertexIndex: Int, target: Vector3f?): Vector3f? {
        if (!hasNormal[vertexIndex]) {
            return null
        }
        val to = target ?: Vector3f()
        to.set(normals[vertexIndex])
        return to
    }

    override fun normalX(vertexIndex: Int): Float {
        return if (hasNormal[vertexIndex]) {
            normals[vertexIndex].x
        } else {
            Float.NaN
        }
    }

    override fun normalY(vertexIndex: Int): Float {
        return if (hasNormal[vertexIndex]) {
            normals[vertexIndex].y
        } else {
            Float.NaN
        }
    }

    override fun normalZ(vertexIndex: Int): Float {
        return if (hasNormal[vertexIndex]) {
            normals[vertexIndex].z
        } else {
            Float.NaN
        }
    }

    override fun lightmap(vertexIndex: Int, lightmap: Int): QuadEmitter {
        hasLightmap[vertexIndex] = true
        this.lightmaps[vertexIndex] = lightmap
        return this
    }

    override fun lightmap(vertexIndex: Int): Int {
        return lightmaps[vertexIndex]
    }

    override fun spriteColor(vertexIndex: Int, spriteIndex: Int, color: Int): QuadEmitter {
        spriteColors[vertexIndex] = color
        return this
    }

    override fun spriteColor(vertexIndex: Int, spriteIndex: Int): Int {
        return spriteColors[vertexIndex]
    }

    override fun spriteU(vertexIndex: Int, spriteIndex: Int): Float {
        return texCoordUs[vertexIndex]
    }

    override fun spriteV(vertexIndex: Int, spriteIndex: Int): Float {
        return texCoordVs[vertexIndex]
    }

    override fun fromVanilla(quadData: IntArray, startIndex: Int, isItem: Boolean): QuadEmitter {
        throw NotImplementedError("TransformingQuadEmitter.fromVanilla")
    }

    override fun fromVanilla(quadData: IntArray?, startIndex: Int): QuadEmitter {
        throw NotImplementedError("TransformingQuadEmitter.fromVanilla")
    }

    override fun fromVanilla(quad: BakedQuad, material: RenderMaterial?, cullFace: Direction?): QuadEmitter {
        val vertexData = quad.vertexData
        val normalI = quad.face.vector
        val normal = Vector3f(normalI.x.toFloat(), normalI.y.toFloat(), normalI.z.toFloat())

        MemoryStack.stackPush().use { memoryStack ->
            val byteBuffer = memoryStack.malloc(VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL.vertexSizeByte)
            val intBuffer = byteBuffer.asIntBuffer()

            for (vertexIndex in 0 until 4) {
                intBuffer.clear()
                intBuffer.put(vertexData, vertexIndex * 8, 8)
                val x = byteBuffer.getFloat(0)
                val y = byteBuffer.getFloat(4)
                val z = byteBuffer.getFloat(8)
                val spriteColor = byteBuffer.getInt(12)
                val u: Float = byteBuffer.getFloat(16)
                val v: Float = byteBuffer.getFloat(20)

                pos(vertexIndex, x, y, z)
                normal(vertexIndex, normal.x, normal.y, normal.z)
                uv(vertexIndex, u, v)
                color(vertexIndex, spriteColor)
            }
        }

        colorIndex(quad.colorIndex)
        nominalFace(quad.face)
        material(material)
        cullFace(cullFace)

        return this
    }

    override fun pos(vertexIndex: Int, x: Float, y: Float, z: Float): QuadEmitter {
        positions[vertexIndex].set(x, y, z)
        return this
    }

    override fun normal(vertexIndex: Int, x: Float, y: Float, z: Float): QuadEmitter {
        hasNormal[vertexIndex] = true
        normals[vertexIndex].set(x, y, z)
        return this
    }

    override fun sprite(vertexIndex: Int, spriteIndex: Int, u: Float, v: Float): QuadEmitter {
        texCoordUs[vertexIndex] = u
        texCoordVs[vertexIndex] = v
        return this
    }

    override fun spriteBake(spriteIndex: Int, sprite: Sprite?, bakeFlags: Int): QuadEmitter {
        this.sprite = sprite
        spriteBakeFlags = bakeFlags
        return this
    }

    override fun spriteBake(sprite: Sprite?, bakeFlags: Int): QuadEmitter {
        this.sprite = sprite
        spriteBakeFlags = bakeFlags
        return this
    }

    override fun color(vertexIndex: Int, color: Int): QuadEmitter {
        spriteColors[vertexIndex] = color
        return this
    }

    override fun color(vertexIndex: Int): Int {
        return spriteColors[vertexIndex]
    }

    override fun u(vertexIndex: Int): Float {
        return texCoordUs[vertexIndex]
    }

    override fun v(vertexIndex: Int): Float {
        return texCoordVs[vertexIndex]
    }

    override fun copyUv(vertexIndex: Int, target: Vector2f?): Vector2f {
        val vec = target ?: Vector2f()
        vec.set(texCoordUs[vertexIndex], texCoordVs[vertexIndex])
        return vec
    }

    override fun uv(vertexIndex: Int, u: Float, v: Float): QuadEmitter {
        texCoordUs[vertexIndex] = u
        texCoordVs[vertexIndex] = v
        return this
    }

    override fun emit(): QuadEmitter {
        if (!transform(this)) {
            clear()
            return this
        }

        copyTo(emitter)
        emitter.material(material)

        emitter.emit()

        clear()

        return this
    }
}
