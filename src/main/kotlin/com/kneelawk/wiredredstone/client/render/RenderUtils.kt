package com.kneelawk.wiredredstone.client.render

import com.kneelawk.wiredredstone.util.requireNonNull
import com.kneelawk.wiredredstone.util.threadLocal
import net.fabricmc.fabric.api.client.model.BakedModelManagerHelper
import net.fabricmc.fabric.api.renderer.v1.RendererAccess
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadView
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.model.BakedModel
import net.minecraft.client.texture.Sprite
import net.minecraft.client.texture.SpriteAtlasTexture
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.Direction
import net.minecraft.util.math.MathHelper.HALF_PI
import net.minecraft.util.math.MathHelper.PI
import net.minecraft.util.math.Quaternion
import net.minecraft.util.math.Vec3f
import java.util.*
import kotlin.math.sqrt

object RenderUtils {
    val MESH_BUILDER: MeshBuilder by threadLocal {
        RendererAccess.INSTANCE.renderer.requireNonNull("Renderer is null").meshBuilder()
    }

    private val MC = MinecraftClient.getInstance()

    private val FLAT_QUATERNION = Quaternion.fromEulerXyz(-HALF_PI, 0f, 0f)

    private val ROTATION_QUATERNIONS = arrayOf(
        Quaternion.IDENTITY,
        Quaternion(Vec3f.POSITIVE_X, PI, false),
        Quaternion(Vec3f.POSITIVE_X, HALF_PI, false),
        Quaternion(Vec3f.POSITIVE_Y, PI, false).apply { hamiltonProduct(Quaternion(Vec3f.POSITIVE_X, HALF_PI, false)) },
        Quaternion(Vec3f.POSITIVE_Y, HALF_PI, false).apply {
            hamiltonProduct(
                Quaternion(Vec3f.POSITIVE_X, HALF_PI, false)
            )
        },
        Quaternion(Vec3f.POSITIVE_Y, -HALF_PI, false).apply {
            hamiltonProduct(
                Quaternion(Vec3f.POSITIVE_X, HALF_PI, false)
            )
        }
    )

    private val CARDINAL_QUATERNIONS = arrayOf(
        Quaternion.IDENTITY,
        Quaternion(Vec3f.POSITIVE_Y, PI, false),
        Quaternion(Vec3f.POSITIVE_Y, HALF_PI, false),
        Quaternion(Vec3f.POSITIVE_Y, -HALF_PI, false)
    )

    fun getBlockSprite(id: Identifier): Sprite {
        return MinecraftClient.getInstance().getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE).apply(id)
    }

    fun getModel(id: Identifier): BakedModel {
        val manager = MinecraftClient.getInstance().bakedModelManager
        return BakedModelManagerHelper.getModel(manager, id) ?: manager.missingModel
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

    fun renderMesh(stack: MatrixStack, consumer: VertexConsumer, mesh: Mesh) {
        val matrix4f = stack.peek().positionMatrix
        val matrix3f = stack.peek().normalMatrix
        mesh.forEach { quad ->
            for (i in 0 until 4) {
                consumer.vertex(matrix4f, quad.x(i), quad.y(i), quad.z(i))
                    .color(quad.spriteColor(i, 0))
                    .texture(quad.spriteU(i, 0), quad.spriteV(i, 0))
                    .overlay(OverlayTexture.DEFAULT_UV)
                    .light(quad.lightmap(i))
                    .normal(matrix3f, quad.normalX(i), quad.normalY(i), quad.normalZ(i))
                    .next()
            }
        }
    }

    fun renderPortText(
        text: Text, side: Direction, rotation: Direction, height: Double, stack: MatrixStack,
        provider: VertexConsumerProvider, light: Int, color: UInt = Colors.WHITE, overline: Boolean = false
    ) {
        stack.push()
        stack.translate(0.5, 0.5, 0.5)
        stack.multiply(rotationQuaternion(side))
        stack.multiply(cardinalQuaternion(rotation))
        stack.translate(-0.5, -0.5, -0.5)

        stack.translate(0.0, height, 0.0)

        stack.multiply(FLAT_QUATERNION)

        stack.scale(1f / 32f, -1f / 32f, 1f / 32f)

        val width = MC.textRenderer.getWidth(text).toDouble()
        val yOffset = if (overline) 2.0 else 0.0
        stack.translate(16.0 - width / 2.0, -(MC.textRenderer.fontHeight.toDouble() + yOffset), 0.0)

        WRTextRenderer.drawText(
            text, color.toInt(), true, overline, stack.peek().positionMatrix, provider, true, 0, light
        )

        stack.pop()
    }

    fun renderOverlayText(
        text: Text, side: Direction, rotation: Direction, x: Double, y: Double, z: Double,
        alignment: HorizontalAlignment, stack: MatrixStack, provider: VertexConsumerProvider, light: Int,
        color: UInt = Colors.WHITE, overline: Boolean = false
    ) {
        stack.push()
        stack.translate(0.5, 0.5, 0.5)
        stack.multiply(rotationQuaternion(side))
        stack.multiply(cardinalQuaternion(rotation))
        stack.translate(-0.5, -0.5, -0.5)

        stack.translate(x, y, z)

        stack.multiply(FLAT_QUATERNION)

        stack.scale(1f / 32f, -1f / 32f, 1f / 32f)

        stack.translate(alignment.offset(MC.textRenderer.getWidth(text)), 0.0, 0.0)

        WRTextRenderer.drawText(
            text, color.toInt(), true, overline, stack.peek().positionMatrix, provider, true, 0, light
        )

        stack.pop()
    }

    fun rotationQuaternion(side: Direction): Quaternion {
        return ROTATION_QUATERNIONS[side.id]
    }

    fun cardinalQuaternion(rotation: Direction): Quaternion {
        return CARDINAL_QUATERNIONS[rotation.id - 2]
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
