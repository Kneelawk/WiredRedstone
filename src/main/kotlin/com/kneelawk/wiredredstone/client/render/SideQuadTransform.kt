package com.kneelawk.wiredredstone.client.render

import com.kneelawk.wiredredstone.util.RotationUtils.rotatedDirection
import com.kneelawk.wiredredstone.util.RotationUtils.rotatedX
import com.kneelawk.wiredredstone.util.RotationUtils.rotatedY
import com.kneelawk.wiredredstone.util.RotationUtils.rotatedZ
import io.vram.frex.api.buffer.QuadEmitter
import io.vram.frex.api.buffer.QuadTransform
import io.vram.frex.api.mesh.QuadView
import io.vram.frex.api.model.InputContext
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3f

/**
 * Transforms quads of something on the DOWN-side into something on the given side.
 */
class SideQuadTransform(val side: Direction) : QuadTransform {
    override fun transform(context: InputContext, input: QuadView, output: QuadEmitter) {
        input.copyTo(output)

        for (i in 0 until 4) {
            val x = input.x(i)
            val y = input.y(i)
            val z = input.z(i)
            output.pos(i, rotatedX(side, x, y), rotatedY(side, y, z), rotatedZ(side, x, y, z))

            if (input.hasNormal(i)) {
                val n = Vec3f()
                input.copyNormal(i, n)
                output.normal(i, rotatedX(side, n.x, n.y), rotatedY(side, n.y, n.z), rotatedZ(side, n.x, n.y, n.z))
            }
        }

        input.cullFace()?.let { cullFace ->
            output.cullFace(rotatedDirection(side, cullFace))
        }

        input.nominalFace()?.let { nominalFace ->
            output.nominalFace(rotatedDirection(side, nominalFace))
        }

        output.emit()
    }
}