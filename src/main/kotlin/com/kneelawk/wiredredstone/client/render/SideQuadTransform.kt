package com.kneelawk.wiredredstone.client.render

import com.kneelawk.wiredredstone.util.RotationUtils.rotatedDirection
import com.kneelawk.wiredredstone.util.RotationUtils.rotatedNormX
import com.kneelawk.wiredredstone.util.RotationUtils.rotatedNormY
import com.kneelawk.wiredredstone.util.RotationUtils.rotatedNormZ
import com.kneelawk.wiredredstone.util.RotationUtils.rotatedPosX
import com.kneelawk.wiredredstone.util.RotationUtils.rotatedPosY
import com.kneelawk.wiredredstone.util.RotationUtils.rotatedPosZ
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext
import net.minecraft.util.math.Direction

/**
 * Transforms quads of something on the DOWN-side into something on the given side.
 */
class SideQuadTransform(val side: Direction) : RenderContext.QuadTransform {
    override fun transform(quad: MutableQuadView): Boolean {
        for (i in 0 until 4) {
            val x = quad.x(i)
            val y = quad.y(i)
            val z = quad.z(i)
            quad.pos(i, rotatedPosX(side, x, y), rotatedPosY(side, y, z), rotatedPosZ(side, x, y, z))

            if (quad.hasNormal(i)) {
                val nx = quad.normalX(i)
                val ny = quad.normalY(i)
                val nz = quad.normalZ(i)
                quad.normal(i, rotatedNormX(side, nx, ny), rotatedNormY(side, ny, nz), rotatedNormZ(side, nx, ny, nz))
            }
        }

        val cullFace = quad.cullFace()
        if (cullFace != null) {
            quad.cullFace(rotatedDirection(side, cullFace))
        }

        val nominalFace = quad.nominalFace()
        if (nominalFace != null) {
            quad.nominalFace(rotatedDirection(side, nominalFace))
        }

        // keep this quad
        return true
    }
}