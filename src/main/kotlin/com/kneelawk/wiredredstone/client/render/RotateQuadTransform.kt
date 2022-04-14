package com.kneelawk.wiredredstone.client.render

import com.kneelawk.wiredredstone.util.RotationUtils
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext
import net.minecraft.util.math.Direction

/**
 * Transforms a model by rotating it around the Y-axis to point in the given direction.
 */
class RotateQuadTransform(val direction: Direction) : RenderContext.QuadTransform {
    override fun transform(quad: MutableQuadView): Boolean {
        for (i in 0 until 4) {
            val x = quad.x(i)
            val y = quad.y(i)
            val z = quad.z(i)
            quad.pos(
                i, RotationUtils.cardinalRotatedX(direction, x, z), y, RotationUtils.cardinalRotatedZ(direction, x, z)
            )

            if (quad.hasNormal(i)) {
                val nx = quad.normalX(i)
                val ny = quad.normalY(i)
                val nz = quad.normalZ(i)
                quad.normal(
                    i, RotationUtils.cardinalRotatedX(direction, nx, nz), ny,
                    RotationUtils.cardinalRotatedZ(direction, nx, nz)
                )
            }
        }

        val cullFace = quad.cullFace()
        if (cullFace != null) {
            quad.cullFace(RotationUtils.cardinalRotatedDirection(direction, cullFace))
        }

        val nominalFace = quad.nominalFace()
        if (nominalFace != null) {
            quad.nominalFace(RotationUtils.cardinalRotatedDirection(direction, nominalFace))
        }

        // keep this quad
        return true
    }
}