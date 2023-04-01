package com.kneelawk.wiredredstone.client.render

import com.kneelawk.wiredredstone.item.GateItem
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.Direction
import net.minecraft.util.math.MathHelper
import org.joml.Vector3f
import org.joml.Vector3fc

object WROutlineRenderer {
    private val PLACEMENT_OUTLINE = arrayOf(
        Line(Vector3f(0f, 1f, 0f), Vector3f(1f, 1f, 0f)),
        Line(Vector3f(0f, 1f, 0f), Vector3f(0f, 1f, 1f)),
        Line(Vector3f(1f, 1f, 0f), Vector3f(1f, 1f, 1f)),
        Line(Vector3f(0f, 1f, 1f), Vector3f(1f, 1f, 1f)),
        Line(Vector3f(0f, 1f, 0f), Vector3f(1f, 1f, 1f)),
        Line(Vector3f(1f, 1f, 0f), Vector3f(0f, 1f, 1f))
    )

    private data class Line(val start: Vector3fc, val end: Vector3fc)

    fun init() {
        WorldRenderEvents.BLOCK_OUTLINE.register(::handleOutline)
    }

    private fun handleOutline(
        renderCtx: WorldRenderContext, outlineCtx: WorldRenderContext.BlockOutlineContext
    ): Boolean {
        val client = MinecraftClient.getInstance()

        client.player?.let { player ->
            (client.crosshairTarget as? BlockHitResult)?.let { target ->
                for (hand in Hand.values()) {
                    val item = player.getStackInHand(hand).item

                    if (item is GateItem) {
                        renderPlacementOutline(renderCtx, outlineCtx, target)

                        return true
                    }
                }
            }
        }

        return true
    }

    private fun renderPlacementOutline(
        renderCtx: WorldRenderContext, outlineCtx: WorldRenderContext.BlockOutlineContext, target: BlockHitResult
    ) {
        val lines = renderCtx.consumers()!!.getBuffer(RenderLayer.LINES)

        val stack = renderCtx.matrixStack()
        stack.push()

        translateForFace(stack, target, outlineCtx)

        stack.translate(0.5, 0.5, 0.5)
        stack.multiply(target.side.rotationQuaternion)
        stack.translate(-0.5, -0.5, -0.5)

        val model = stack.peek().positionMatrix
        val normal = stack.peek().normalMatrix

        for (line in PLACEMENT_OUTLINE) {
            var dx = line.end.x() - line.start.x()
            var dy = line.end.y() - line.start.y()
            var dz = line.end.z() - line.start.z()
            val invLen = MathHelper.inverseSqrt(dx * dx + dy * dy + dz * dz)
            dx *= invLen
            dy *= invLen
            dz *= invLen

            lines.vertex(model, line.start.x(), line.start.y(), line.start.z()).color(0f, 0f, 0f, 0.4f)
                .normal(normal, dx, dy, dz).next()
            lines.vertex(model, line.end.x(), line.end.y(), line.end.z()).color(0f, 0f, 0f, 0.4f)
                .normal(normal, dx, dy, dz).next()
        }

        stack.pop()
    }

    private fun translateForFace(
        stack: MatrixStack, target: BlockHitResult, outlineCtx: WorldRenderContext.BlockOutlineContext
    ) {
        val blockPos = outlineCtx.blockPos()
        val hitPos = target.pos
        when (target.side!!) {
            Direction.DOWN -> {
                stack.translate(
                    blockPos.x.toDouble() - outlineCtx.cameraX(),
                    hitPos.y - outlineCtx.cameraY(),
                    blockPos.z.toDouble() - outlineCtx.cameraZ()
                )
            }
            Direction.UP -> {
                stack.translate(
                    blockPos.x.toDouble() - outlineCtx.cameraX(),
                    hitPos.y - 1.0 - outlineCtx.cameraY(),
                    blockPos.z.toDouble() - outlineCtx.cameraZ()
                )
            }
            Direction.NORTH -> {
                stack.translate(
                    blockPos.x.toDouble() - outlineCtx.cameraX(),
                    blockPos.y.toDouble() - outlineCtx.cameraY(),
                    hitPos.z - outlineCtx.cameraZ()
                )
            }
            Direction.SOUTH -> {
                stack.translate(
                    blockPos.x.toDouble() - outlineCtx.cameraX(),
                    blockPos.y.toDouble() - outlineCtx.cameraY(),
                    hitPos.z - 1.0 - outlineCtx.cameraZ()
                )
            }
            Direction.WEST -> {
                stack.translate(
                    hitPos.x - outlineCtx.cameraX(),
                    blockPos.y.toDouble() - outlineCtx.cameraY(),
                    blockPos.z.toDouble() - outlineCtx.cameraZ()
                )
            }
            Direction.EAST -> {
                stack.translate(
                    hitPos.x - 1.0 - outlineCtx.cameraX(),
                    blockPos.y.toDouble() - outlineCtx.cameraY(),
                    blockPos.z.toDouble() - outlineCtx.cameraZ()
                )
            }
        }
    }

}
