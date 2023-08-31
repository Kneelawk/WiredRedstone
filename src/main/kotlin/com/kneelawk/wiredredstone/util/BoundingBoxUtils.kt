package com.kneelawk.wiredredstone.util

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import com.kneelawk.wiredredstone.util.bits.CenterConnectionUtils
import com.kneelawk.wiredredstone.util.bits.ConnectionUtils
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import java.util.EnumMap

object BoundingBoxUtils {
    data class ShapeKey(val side: Direction, val connections: UByte)

    private fun getWireShape(wireWidth: Double, wireHeight: Double, key: ShapeKey): VoxelShape {
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
                connections, wireWidth, Direction.NORTH, 0.0
            )
            val (doXNeg, xNegEnd) = calculateConnection(
                connections, wireWidth, Direction.WEST, 0.0
            )
            val (doZPos, zPosEnd) = calculateConnection(
                connections, wireWidth, Direction.SOUTH, 16.0
            )
            val (doXPos, xPosEnd) = calculateConnection(
                connections, wireWidth, Direction.EAST, 16.0
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
        connections: UByte, wireWidth: Double, cardinal: Direction, end: Double
    ): Pair<Boolean, Double> {
        return if (!ConnectionUtils.isDisconnected(connections, cardinal)) {
            Pair(true, end)
        } else {
            Pair(
                false,
                8f + if (cardinal == Direction.NORTH || cardinal == Direction.WEST) -wireWidth / 2f else wireWidth / 2f
            )
        }
    }

    private fun getCenterWireShape(wireDiameter: Double, connections: UByte): VoxelShape {
        val base = Box(
            0.5 - wireDiameter / 32.0, 0.5 - wireDiameter / 32.0, 0.5 - wireDiameter / 32.0, 0.5 + wireDiameter / 32.0,
            0.5 + wireDiameter / 32.0, 0.5 + wireDiameter / 32.0
        ).vs()

        val limb = Box(
            0.5 - wireDiameter / 32.0, 0.0, 0.5 - wireDiameter / 32.0, 0.5 + wireDiameter / 32.0,
            0.5 - wireDiameter / 32.0, 0.5 + wireDiameter / 32.0
        )

        var shape = base
        for (dir in Direction.values()) {
            if (CenterConnectionUtils.test(connections, dir)) {
                shape = VoxelShapes.union(shape, RotationUtils.rotatedBox(dir, limb).vs())
            }
        }

        return shape
    }

    fun getWireOutlineShapes(wireWidth: Double, wireHeight: Double): LoadingCache<ShapeKey, VoxelShape> {
        return CacheBuilder.newBuilder().build(CacheLoader.from { key -> getWireShape(wireWidth, wireHeight, key) })
    }

    fun getCenterWireOutlineShapes(wireDiameter: Double): LoadingCache<UByte, VoxelShape> {
        return CacheBuilder.newBuilder()
            .build(CacheLoader.from { connections -> getCenterWireShape(wireDiameter, connections) })
    }

    fun getRotatedShapes(base: Box): EnumMap<Direction, VoxelShape> {
        val map = EnumMap<Direction, VoxelShape>(Direction::class.java)
        for (dir in Direction.values()) {
            map[dir] = VoxelShapes.cuboid(RotationUtils.rotatedBox(dir, base))
        }
        return map
    }

    fun getOrientedShapes(base: Box): Map<SidedOrientation, VoxelShape> {
        val map = mutableMapOf<SidedOrientation, VoxelShape>()
        for (side in Direction.values()) {
            for (direction in DirectionUtils.HORIZONTALS) {
                val orientation = SidedOrientation(side, direction)
                map[orientation] = VoxelShapes.cuboid(
                    RotationUtils.rotatedBox(side, RotationUtils.cardinalRotatedBox(direction, base))
                )
            }
        }
        return map
    }

    fun getOrientedShapes(shape: VoxelShape): Map<SidedOrientation, VoxelShape> {
        val acc = mutableMapOf<SidedOrientation, VoxelShape>()
        shape.boundingBoxes.map { getOrientedShapes(it) }
            .forEach { map -> acc.mergeAll(map, VoxelShapes::union) }
        return acc
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

    fun getCenterWireInsideConnectionShape(dir: Direction, wireDiameter: Double): Box {
        return RotationUtils.rotatedBox(
            dir, Box(
                0.5 - wireDiameter / 32.0, 0.0, 0.5 - wireDiameter / 32.0, 0.5 + wireDiameter / 32.0,
                0.5 - wireDiameter / 32.0, 0.5 + wireDiameter / 32.0
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

    fun isTouching(hitVec: Vec3d, shape: VoxelShape): Boolean {
        for (box in shape.boundingBoxes) {
            if (box.expand(0.01).contains(hitVec)) {
                return true
            }
        }
        return false
    }
}
