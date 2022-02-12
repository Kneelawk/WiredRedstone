package com.kneelawk.wiredredstone.client.render

import com.kneelawk.wiredredstone.util.RotationUtils.cardinalRotatedDirection
import com.kneelawk.wiredredstone.util.RotationUtils.cardinalRotatedX
import com.kneelawk.wiredredstone.util.RotationUtils.cardinalRotatedZ
import io.vram.frex.api.buffer.QuadEmitter
import io.vram.frex.api.buffer.QuadTransform
import io.vram.frex.api.mesh.QuadView
import io.vram.frex.api.model.InputContext
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3f

/**
 * Transforms a model by rotating it around the Y-axis to point in the given direction.
 */
class RotateQuadTransform(val direction: Direction) : QuadTransform {
    override fun transform(context: InputContext, input: QuadView, output: QuadEmitter) {
        input.copyTo(output)

        for (i in 0 until 4) {
            val x = input.x(i)
            val y = input.y(i)
            val z = input.z(i)
            output.pos(i, cardinalRotatedX(direction, x, z), y, cardinalRotatedZ(direction, x, z))

            if (input.hasNormal(i)) {
                val n = Vec3f()
                input.copyNormal(i, n)
                output.normal(i, cardinalRotatedX(direction, n.x, n.z), n.y, cardinalRotatedZ(direction, n.x, n.z))
            }
        }

        input.cullFace()?.let { cullFace ->
            output.cullFace(cardinalRotatedDirection(direction, cullFace))
        }

        input.nominalFace()?.let { nominalFace ->
            output.nominalFace(cardinalRotatedDirection(direction, nominalFace))
        }

        output.emit()
    }
}