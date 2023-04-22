package com.kneelawk.wiredredstone.logic

import alexiil.mc.lib.multipart.api.MultipartUtil
import com.kneelawk.graphlib.api.v1.util.SidedPos
import com.kneelawk.wiredredstone.part.BundledPowerablePart
import com.kneelawk.wiredredstone.util.DirectionUtils
import com.kneelawk.wiredredstone.util.RotationUtils
import com.kneelawk.wiredredstone.util.bits.BlockageUtils
import com.kneelawk.wiredredstone.util.bits.ConnectionUtils
import com.kneelawk.wiredredstone.util.constrainedMaxOf
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.DyeColor
import net.minecraft.util.math.Direction
import net.minecraft.world.World
import java.util.*

object BundledCableLogic {
    private val powerSources = mutableListOf<BundledPowerSource>()
    private val connectionFinders = mutableListOf<BundledConnectionFinder>()

    @JvmStatic
    fun registerPowerSource(source: BundledPowerSource) {
        powerSources.add(source)
    }

    @JvmStatic
    fun registerConnectionFinder(finder: BundledConnectionFinder) {
        connectionFinders.add(finder)
    }

    fun getBundledCableInput(world: ServerWorld, pos: SidedPos, connections: UByte, blockage: UByte): ULong {
        return receivingSides(pos, connections, blockage).map {
            getSingleBundledCableInput(world, SidedPos(pos.pos, it))
        }.maxPower()
    }

    fun getBundledCableInput(
        world: ServerWorld, pos: SidedPos, inner: DyeColor, connections: UByte, blockage: UByte
    ): Int {
        return receivingSides(pos, connections, blockage).constrainedMaxOf(0, 15) {
            getSingleBundledCableInput(world, SidedPos(pos.pos, it), inner)
        }
    }

    private fun receivingSides(pos: SidedPos, connections: UByte, blockage: UByte): Sequence<Direction> {
        return Direction.values().asSequence().filter { a ->
            val cardinal = RotationUtils.unrotatedDirection(pos.side, a)
            a == pos.side || (DirectionUtils.isHorizontal(cardinal) && ConnectionUtils.isExternal(
                connections, cardinal
            ) && !BlockageUtils.isBlocked(blockage, cardinal))
        }
    }

    private fun getSingleBundledCableInput(world: ServerWorld, pos: SidedPos): ULong {
        return powerSources.asSequence().map { it.getBundledPower(world, pos) }.maxPower()
    }

    private fun getSingleBundledCableInput(world: ServerWorld, pos: SidedPos, inner: DyeColor): Int {
        return powerSources.asSequence().constrainedMaxOf(0, 15) { get(it.getBundledPower(world, pos), inner) }
    }

    fun getBundledCableOutput(world: World, pos: SidedPos): ULong? {
        val multipart = MultipartUtil.get(world, pos.pos) ?: return null
        val parts = multipart.getAllParts { it is BundledPowerablePart }

        if (parts.isEmpty()) {
            return null
        }

        return parts.asSequence().map { (it as BundledPowerablePart).getPower(pos.side) }.maxPower()
    }

    fun hasBundledCableOutput(world: World, pos: SidedPos): Boolean {
        return connectionFinders.any { it.hasBundledConnection(world, pos) }
    }

    @JvmStatic
    fun getBundledCableOutputOptional(world: World, pos: SidedPos): OptionalLong {
        val multipart = MultipartUtil.get(world, pos.pos) ?: return OptionalLong.empty()
        val parts = multipart.getAllParts { it is BundledPowerablePart }

        if (parts.isEmpty()) {
            return OptionalLong.empty()
        }

        return OptionalLong.of(
            parts.asSequence().map { (it as BundledPowerablePart).getPower(pos.side) }.maxPower().toLong()
        )
    }

    @JvmStatic
    fun get(power: ULong, inner: DyeColor): Int {
        return ((power shr (inner.id shl 2)) and 0xFuL).toInt()
    }

    @JvmStatic
    fun set(power: ULong, inner: DyeColor, innerPower: Int): ULong {
        return (power and mask(inner.id).inv()) or ((innerPower and 0xF).toULong() shl (inner.id shl 2))
    }

    @JvmStatic
    fun analog2Digital(power: ULong): UShort {
        var res: UShort = 0u

        for (i in 0 until 16) {
            val mask = mask(i)
            if (power and mask != 0uL) {
                res = res or (1u shl i).toUShort()
            }
        }

        return res
    }

    @JvmStatic
    fun digital2Analog(signals: UShort): ULong {
        var res: ULong = 0uL

        for (i in 0 until 16) {
            if (signals and (1u shl i).toUShort() != 0u.toUShort()) {
                res = res or mask(i)
            }
        }

        return res
    }

    private fun Sequence<ULong>.maxPower(): ULong {
        return fold(0uL) { acc, input ->
            var res = acc

            for (i in 0 until 16) {
                val mask = mask(i)
                if (acc and mask < input and mask) {
                    res = (res and mask.inv()) or (input and mask)
                }
            }

            res
        }
    }

    private fun mask(index: Int): ULong = 0xFuL shl (index shl 2)
}
