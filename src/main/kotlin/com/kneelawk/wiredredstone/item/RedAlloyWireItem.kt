package com.kneelawk.wiredredstone.item

import alexiil.mc.lib.multipart.api.MultipartHolder
import alexiil.mc.lib.multipart.api.MultipartUtil
import com.kneelawk.wiredredstone.part.RedAlloyWirePart
import com.kneelawk.wiredredstone.part.WRParts
import com.kneelawk.wiredredstone.util.WireUtils.isValidFace
import net.minecraft.item.Item
import net.minecraft.item.ItemUsageContext
import net.minecraft.util.ActionResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import net.minecraft.world.BlockView
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor

class RedAlloyWireItem(settings: Settings) : Item(settings) {
    override fun useOnBlock(context: ItemUsageContext): ActionResult {
        val world = context.world
        if (world.isClient) {
            return ActionResult.PASS
        }

        val pos = context.blockPos
        val side = context.side
        val hitPos = context.hitPos.subtract(Vec3d.ofCenter(pos))
        val offsetPos = pos.offset(side)
        val state = world.getBlockState(pos)
        val sideSide = closestSideSide(hitPos, side)

        val offer = if (isOnExternalSide(hitPos, side)) {
            if (isValidFace(state, world, pos, side)) {
                MultipartUtil.offerNewPart(world, offsetPos, creator(side.opposite)) ?: return ActionResult.FAIL
            } else {
                val sidePos = offsetPos.offset(sideSide)
                val sideState = world.getBlockState(sidePos)
                if (isValidFace(sideState, world, sidePos, sideSide.opposite)) {
                    MultipartUtil.offerNewPart(world, offsetPos, creator(sideSide)) ?: return ActionResult.FAIL
                } else {
                    findValidFace(world, offsetPos)?.let {
                        MultipartUtil.offerNewPart(world, offsetPos, creator(it))
                    } ?: return ActionResult.FAIL
                }
            }
        } else {
            val offsetState = world.getBlockState(offsetPos)
            if (isValidFace(offsetState, world, offsetPos, side.opposite)) {
                MultipartUtil.offerNewPart(world, pos, creator(side)) ?: return ActionResult.FAIL
            } else {
                val sidePos1 = pos.offset(sideSide)
                val sideState1 = world.getBlockState(sidePos1)
                (if (isValidFace(sideState1, world, sidePos1, sideSide.opposite)) {
                    MultipartUtil.offerNewPart(world, pos, creator(sideSide))
                } else null) ?: run {
                    val sidePos2 = offsetPos.offset(sideSide)
                    val sideState2 = world.getBlockState(sidePos2)
                    if (isValidFace(sideState2, world, sidePos2, sideSide.opposite)) {
                        MultipartUtil.offerNewPart(world, offsetPos, creator(sideSide)) ?: return ActionResult.FAIL
                    } else {
                        findValidFace(world, offsetPos)?.let {
                            MultipartUtil.offerNewPart(world, offsetPos, creator(it))
                        } ?: return ActionResult.FAIL
                    }
                }
            }
        }

        offer.apply()
        offer.holder.part.onPlacedBy(context.player, context.hand)
        context.stack.decrement(1)

        return ActionResult.SUCCESS
    }

    private fun closestSideSide(hitPos: Vec3d, hitSide: Direction): Direction {
        val zeroed = hitPos.withAxis(hitSide.axis, 0.0)
        return Direction.getFacing(zeroed.x, zeroed.y, zeroed.z)
    }

    private fun isOnExternalSide(hitPos: Vec3d, hitSide: Direction): Boolean {
        val d = hitPos.getComponentAlongAxis(hitSide.axis) + 0.5
        return almostEquals(d, floor(d)) || almostEquals(d, ceil(d))
    }

    private fun almostEquals(d1: Double, d2: Double): Boolean {
        return abs(d1 - d2) < 0.001
    }

    private fun findValidFace(world: BlockView, pos: BlockPos): Direction? {
        for (side in Direction.values()) {
            val sidePos = pos.offset(side)
            val sideState = world.getBlockState(sidePos)
            if (isValidFace(sideState, world, sidePos, side.opposite)) {
                return side
            }
        }
        return null
    }

    private fun creator(side: Direction): ((MultipartHolder) -> RedAlloyWirePart) {
        return { holder ->
            RedAlloyWirePart(WRParts.RED_ALLOY_WIRE, holder, side, 0u, false)
        }
    }
}