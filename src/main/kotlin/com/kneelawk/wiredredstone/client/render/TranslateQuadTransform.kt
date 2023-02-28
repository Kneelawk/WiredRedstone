package com.kneelawk.wiredredstone.client.render

import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext
import org.joml.Vector3f

class TranslateQuadTransform(private val translation: Vector3f) : RenderContext.QuadTransform {
    override fun transform(quad: MutableQuadView): Boolean {
        for (i in 0 until 4) {
            val x = quad.x(i)
            val y = quad.y(i)
            val z = quad.z(i)
            quad.pos(i, x + translation.x, y + translation.y, z + translation.z)
        }

        return true
    }
}
