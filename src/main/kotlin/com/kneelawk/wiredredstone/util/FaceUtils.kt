package com.kneelawk.wiredredstone.util

import net.minecraft.util.math.Direction

object FaceUtils {
    private val FACES_FOR_SIDE = arrayOf(
        arrayOf(
            floatArrayOf(0f, 0f, 1f),
            floatArrayOf(0f, 0f, 0f),
            floatArrayOf(1f, 0f, 0f),
            floatArrayOf(1f, 0f, 1f)
        ),
        arrayOf(
            floatArrayOf(0f, 1f, 0f),
            floatArrayOf(0f, 1f, 1f),
            floatArrayOf(1f, 1f, 1f),
            floatArrayOf(1f, 1f, 0f)
        ),
        arrayOf(
            floatArrayOf(1f, 1f, 0f),
            floatArrayOf(1f, 0f, 0f),
            floatArrayOf(0f, 0f, 0f),
            floatArrayOf(0f, 1f, 0f)
        ),
        arrayOf(
            floatArrayOf(0f, 1f, 1f),
            floatArrayOf(0f, 0f, 1f),
            floatArrayOf(1f, 0f, 1f),
            floatArrayOf(1f, 1f, 1f)
        ),
        arrayOf(
            floatArrayOf(0f, 1f, 0f),
            floatArrayOf(0f, 0f, 0f),
            floatArrayOf(0f, 0f, 1f),
            floatArrayOf(0f, 1f, 1f)
        ),
        arrayOf(
            floatArrayOf(1f, 1f, 1f),
            floatArrayOf(1f, 0f, 1f),
            floatArrayOf(1f, 0f, 0f),
            floatArrayOf(1f, 1f, 0f)
        )
    )

    fun getFaceForSide(side: Direction): Array<FloatArray> = FACES_FOR_SIDE[side.id]
}
