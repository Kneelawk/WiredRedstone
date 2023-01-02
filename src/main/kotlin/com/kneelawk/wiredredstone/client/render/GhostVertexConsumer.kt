package com.kneelawk.wiredredstone.client.render

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.VertexConsumer
import kotlin.math.sin

class GhostVertexConsumer(private val delegate: VertexConsumer) : VertexConsumer {
    companion object {
        private var alpha = 255

        fun init() {
            WorldRenderEvents.START.register { ctx ->
                MinecraftClient.getInstance().player?.let { player ->
                    val placementDelta = (player.world.time % 100) + ctx.tickDelta()
                    alpha = ((sin(placementDelta / 4f) / 4f + 0.5f) * 255f + 0.5f).toInt()
                }
            }
        }
    }

    override fun vertex(x: Double, y: Double, z: Double): VertexConsumer {
        delegate.vertex(x, y, z)
        return this
    }

    override fun color(red: Int, green: Int, blue: Int, alpha: Int): VertexConsumer {
        delegate.color(red, green, blue, GhostVertexConsumer.alpha)
        return this
    }

    override fun texture(u: Float, v: Float): VertexConsumer {
        delegate.texture(u, v)
        return this
    }

    override fun overlay(u: Int, v: Int): VertexConsumer {
        delegate.overlay(u, v)
        return this
    }

    override fun light(u: Int, v: Int): VertexConsumer {
        delegate.light(u, v)
        return this
    }

    override fun normal(x: Float, y: Float, z: Float): VertexConsumer {
        delegate.normal(x, y, z)
        return this
    }

    override fun next() {
        delegate.next()
    }

    override fun fixedColor(red: Int, green: Int, blue: Int, alpha: Int) {
        delegate.fixedColor(red, green, blue, GhostVertexConsumer.alpha)
    }

    override fun unfixColor() {
        delegate.unfixColor()
    }
}
