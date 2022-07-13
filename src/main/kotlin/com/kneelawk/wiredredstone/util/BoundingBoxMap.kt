package com.kneelawk.wiredredstone.util

import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import net.minecraft.util.shape.VoxelShape

class BoundingBoxMap<K>(private val shapes: List<Pair<K, Map<SidedOrientation, VoxelShape>>>) {
    companion object {
        fun <K> ofBoxes(vararg pairs: Pair<K, Box>): BoundingBoxMap<K> {
            return BoundingBoxMap(pairs.map { p -> p.first to BoundingBoxUtils.getOrientedShapes(p.second) })
        }

        fun <K> ofVoxelShapes(vararg pairs: Pair<K, VoxelShape>): BoundingBoxMap<K> {
            return BoundingBoxMap(pairs.map { p -> p.first to BoundingBoxUtils.getOrientedShapes(p.second) })
        }
    }

    fun getTouching(hitVec: Vec3d, orientation: SidedOrientation): Touching<K>? {
        for (pair in shapes) {
            val shape = pair.second[orientation]
            if (shape != null && BoundingBoxUtils.isTouching(hitVec, shape)) {
                return Touching(pair.first, shape)
            }
        }
        return null
    }

    data class Touching<K>(val key: K, val shape: VoxelShape)
}
