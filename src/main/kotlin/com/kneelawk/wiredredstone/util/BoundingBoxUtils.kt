package com.kneelawk.wiredredstone.util

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import java.util.*

object BoundingBoxUtils {
    data class ShapeKey(val side: Direction, val connections: UByte)

    private fun getWireShape(wireWidth: Double, wireHeight: Double, key: ShapeKey): VoxelShape {
        val axis = key.side.axis
        val connections = key.connections

        if (connections == 0u.toUByte()) {
            return VoxelShapes.cuboid(
                RotationUtils.rotatedBox(
                    key.side, Box(
                        0.5 - wireWidth / 32.0, 0.0, 0.5 - wireWidth / 32.0,
                        0.5 + wireWidth / 32.0, wireHeight / 16.0, 0.5 + wireWidth / 32.0
                    )
                )
            )
        } else {
            val (doZNeg, zNegEnd) = calculateConnection(
                connections, axis, wireWidth, Direction.NORTH, wireHeight, 0.0, Direction.Axis.Y, true
            )
            val (doXNeg, xNegEnd) = calculateConnection(
                connections, axis, wireWidth, Direction.WEST, wireHeight, 0.0, Direction.Axis.X, false
            )
            val (doZPos, zPosEnd) = calculateConnection(
                connections, axis, wireWidth, Direction.SOUTH, 16.0 - wireHeight, 16.0, Direction.Axis.Y, true
            )
            val (doXPos, xPosEnd) = calculateConnection(
                connections, axis, wireWidth, Direction.EAST, 16.0 - wireHeight, 16.0, Direction.Axis.X, false
            )

            var shape = VoxelShapes.empty()

            if (doXNeg || doXPos) {
                shape = VoxelShapes.cuboid(
                    RotationUtils.rotatedBox(
                        key.side, Box(
                            xNegEnd / 16.0, 0.0, 0.5 - wireWidth / 32.0, xPosEnd / 16.0, wireHeight / 16.0,
                            0.5 + wireWidth / 32.0
                        )
                    )
                )
            }

            if (doZNeg || doZPos) {
                shape = VoxelShapes.union(
                    shape, VoxelShapes.cuboid(
                        RotationUtils.rotatedBox(
                            key.side, Box(
                                0.5 - wireWidth / 32.0, 0.0, zNegEnd / 16.0, 0.5 + wireWidth / 32.0, wireHeight / 16.0,
                                zPosEnd / 16.0
                            )
                        )
                    )
                )
            }

            return shape
        }
    }

    private fun calculateConnection(
        connections: UByte, axis: Direction.Axis, wireWidth: Double, cardinal: Direction, internalEnd: Double,
        externalEnd: Double, specialAxis: Direction.Axis, axisIsLarger: Boolean
    ): Pair<Boolean, Double> {
        return if (ConnectionUtils.isInternal(connections, cardinal)) {
            Pair(true, if ((axis == specialAxis) == axisIsLarger) externalEnd else internalEnd)
        } else if (ConnectionUtils.isExternal(connections, cardinal) || ConnectionUtils.isCorner(
                connections, cardinal
            )
        ) {
            Pair(true, externalEnd)
        } else {
            Pair(
                false,
                8f + if (cardinal == Direction.NORTH || cardinal == Direction.WEST) -wireWidth / 2f else wireWidth / 2f
            )
        }
    }

    fun getWireOutlineShapes(wireWidth: Double, wireHeight: Double): LoadingCache<ShapeKey, VoxelShape> {
        return CacheBuilder.newBuilder().build(CacheLoader.from { key -> getWireShape(wireWidth, wireHeight, key) })
    }

    fun getRotatedShapes(base: Box): EnumMap<Direction, VoxelShape> {
        val map = EnumMap<Direction, VoxelShape>(Direction::class.java)
        for (dir in Direction.values()) {
            map[dir] = VoxelShapes.cuboid(RotationUtils.rotatedBox(dir, base))
        }
        return map
    }

    fun getWireConflictShapes(wireWidth: Double, wireHeight: Double): EnumMap<Direction, VoxelShape> {
        return getRotatedShapes(
            Box(
                0.5 - wireWidth / 32.0, 0.0, 0.5 - wireWidth / 32.0,
                0.5 + wireWidth / 32.0, wireHeight / 16.0, 0.5 + wireWidth / 32.0
            )
        )
    }

    fun getWireInsideConnectionShape(
        side: Direction, cardinal: Direction, wireWidth: Double, wireHeight: Double
    ): Box? {
        if (cardinal.horizontal < 0) {
            return null
        }

        return RotationUtils.rotatedBox(
            side, RotationUtils.cardinalRotatedBox(
                cardinal, Box(
                    0.5 - wireWidth / 32.0, 0.0, 0.0, 0.5 + wireWidth / 32.0, wireHeight / 16.0, 0.5 - wireWidth / 32.0
                )
            )
        )
    }

    fun getWireOutsideConnectionShape(
        side: Direction, cardinal: Direction, wireWidth: Double, wireHeight: Double, shouldBeInsideBlock: Boolean
    ): Box? {
        if (cardinal.axis == Direction.Axis.Y) {
            return null
        }

        return RotationUtils.rotatedBox(
            side, RotationUtils.cardinalRotatedBox(
                cardinal, if (shouldBeInsideBlock) {
                    Box(
                        0.5 - wireWidth / 32.0,
                        0.0,
                        1.0 - wireHeight / 16.0,
                        0.5 + wireWidth / 32.0,
                        wireHeight / 16.0,
                        1.0
                    )
                } else {
                    Box(
                        0.5 - wireWidth / 32.0,
                        0.0,
                        -wireHeight / 16.0,
                        0.5 + wireWidth / 32.0,
                        wireHeight / 16.0,
                        0.0
                    )
                }
            )
        )
    }
}