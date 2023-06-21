package com.kneelawk.wiredredstone.client.render

import com.kneelawk.graphlib.api.client.render.RenderUtils
import com.kneelawk.wiredredstone.node.PowerlineConnectorBlockNode
import com.kneelawk.wiredredstone.node.WRBlockNodes
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.minecraft.client.render.RenderLayer

object WRPowerlineRenderer {
    fun init() {
        WorldRenderEvents.AFTER_ENTITIES.register(::render)
    }

    fun render(ctx: WorldRenderContext) {
        val view = WRBlockNodes.WIRE_NET.clientGraphView ?: return
        val consumers = ctx.consumers() ?: return
        val stack = ctx.matrixStack()
        val cameraPos = ctx.camera().pos

        stack.push()
        stack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z)

        for (graph in view.allGraphs) {
            for (entity in graph.linkEntities) {
                val context = entity.context
                val pos1 = context.firstBlockPos
                val node1 = context.firstNode as? PowerlineConnectorBlockNode ?: continue
                val side1 = node1.side
                val pos2 = context.secondBlockPos
                val node2 = context.secondNode as? PowerlineConnectorBlockNode ?: continue
                val side2 = node2.side

                RenderUtils.drawLine(
                    ctx.matrixStack(), consumers.getBuffer(RenderLayer.LINES),
                    pos1.x + 0.5f + side1.offsetX * -0.0625f,
                    pos1.y + 0.5f + side1.offsetY * -0.0625f,
                    pos1.z + 0.5f + side1.offsetZ * -0.0625f,
                    pos2.x + 0.5f + side2.offsetX * -0.0625f,
                    pos2.y + 0.5f + side2.offsetY * -0.0625f,
                    pos2.z + 0.5f + side2.offsetZ * -0.0625f,
                    0xFFFFFFFF.toInt()
                )
            }
        }

        stack.pop()
    }
}
