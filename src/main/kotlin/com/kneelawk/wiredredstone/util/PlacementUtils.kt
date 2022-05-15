package com.kneelawk.wiredredstone.util

import alexiil.mc.lib.multipart.api.AbstractPart
import alexiil.mc.lib.multipart.api.MultipartContainer
import alexiil.mc.lib.multipart.api.MultipartHolder
import alexiil.mc.lib.multipart.api.MultipartUtil
import net.minecraft.block.BlockState
import net.minecraft.item.ItemUsageContext
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import net.minecraft.world.BlockView
import net.minecraft.world.World
import kotlin.math.abs

object PlacementUtils {
    fun finishPlacement(
        context: ItemUsageContext, offer: MultipartContainer.PartOffer? = null, closestBlockState: BlockState
    ) {
        val soundGroup = closestBlockState.soundGroup
        finishPlacement(context, offer, soundGroup.placeSound, soundGroup.volume, soundGroup.pitch)
    }

    fun finishPlacement(
        context: ItemUsageContext, offer: MultipartContainer.PartOffer? = null, placeSound: SoundEvent, volume: Float,
        pitch: Float
    ) {
        val player = context.player
        if (player == null || !player.abilities.creativeMode) {
            context.stack.decrement(1)
        }

        context.world.playSound(
            null,
            context.blockPos,
            placeSound,
            SoundCategory.BLOCKS,
            (volume + 1f) / 2f,
            pitch * 0.8f
        )

        offer?.let {
            it.apply()
            it.holder.part.onPlacedBy(player, context.hand)
        }
    }

    fun tryPlaceWire(
        context: ItemUsageContext, creatorFactory: (Direction) -> ((MultipartHolder) -> AbstractPart)
    ): MultipartContainer.PartOffer? {
        val world = context.world
        val pos = context.blockPos
        val side = context.side
        val hitPos = context.hitPos.subtract(Vec3d.ofCenter(pos))
        val offsetPos = pos.offset(side)
        val state = world.getBlockState(pos)
        val sideSide = closestSideSide(hitPos, side)

        return if (isOnExternalSide(hitPos, side)) {
            (if (ConnectableUtils.isValidFace(state, world, pos, side)) {
                MultipartUtil.offerNewPart(world, offsetPos, creatorFactory(side.opposite))
            } else null) ?: tryOffsetSide(offsetPos, sideSide, world, creatorFactory)
        } else {
            val offsetState = world.getBlockState(offsetPos)
            if (ConnectableUtils.isValidFace(offsetState, world, offsetPos, side.opposite)) {
                MultipartUtil.offerNewPart(world, pos, creatorFactory(side))
            } else {
                val sidePos1 = pos.offset(sideSide)
                val sideState1 = world.getBlockState(sidePos1)
                (if (ConnectableUtils.isValidFace(sideState1, world, sidePos1, sideSide.opposite)) {
                    MultipartUtil.offerNewPart(world, pos, creatorFactory(sideSide))
                } else null) ?: tryOffsetSide(offsetPos, sideSide, world, creatorFactory)
            }
        }
    }

    fun tryPlaceGate(
        context: ItemUsageContext, creatorFactory: (Direction, Direction) -> (MultipartHolder) -> AbstractPart
    ): MultipartContainer.PartOffer? {
        val world = context.world
        val pos = context.blockPos
        val side = context.side
        val hitPos = context.hitPos.subtract(Vec3d.ofCenter(pos))
        val offsetPos = pos.offset(side)
        val state = world.getBlockState(pos)
        val sideSide = closestSideSide(hitPos, side)
        val direction = DirectionUtils.makeHorizontal(RotationUtils.unrotatedDirection(side.opposite, sideSide))

        return if (ConnectableUtils.isValidFace(state, world, pos, side)) {
            MultipartUtil.offerNewPart(world, offsetPos, creatorFactory(side.opposite, direction))
        } else null
    }

    private fun tryOffsetSide(
        offsetPos: BlockPos, sideSide: Direction,
        world: World, creatorFactory: (Direction) -> ((MultipartHolder) -> AbstractPart)
    ): MultipartContainer.PartOffer? {
        val sidePos = offsetPos.offset(sideSide)
        val sideState = world.getBlockState(sidePos)
        return if (ConnectableUtils.isValidFace(sideState, world, sidePos, sideSide.opposite)) {
            MultipartUtil.offerNewPart(world, offsetPos, creatorFactory(sideSide))
        } else {
            findValidFace(world, offsetPos)?.let {
                MultipartUtil.offerNewPart(world, offsetPos, creatorFactory(it))
            }
        }
    }

    private fun closestSideSide(hitPos: Vec3d, hitSide: Direction): Direction {
        val zeroed = hitPos.withAxis(hitSide.axis, 0.0)
        return Direction.getFacing(zeroed.x, zeroed.y, zeroed.z)
    }

    private fun isOnExternalSide(hitPos: Vec3d, hitSide: Direction): Boolean {
        val d = hitPos.getComponentAlongAxis(hitSide.axis)
        return almostEquals(d, -0.5) || almostEquals(d, 0.5)
    }

    private fun almostEquals(d1: Double, d2: Double): Boolean {
        return abs(d1 - d2) < 0.001
    }

    private fun findValidFace(world: BlockView, pos: BlockPos): Direction? {
        for (side in Direction.values()) {
            val sidePos = pos.offset(side)
            val sideState = world.getBlockState(sidePos)
            if (ConnectableUtils.isValidFace(sideState, world, sidePos, side.opposite)) {
                return side
            }
        }
        return null
    }
}