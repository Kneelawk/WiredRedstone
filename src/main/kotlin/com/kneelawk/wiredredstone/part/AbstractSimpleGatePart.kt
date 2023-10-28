package com.kneelawk.wiredredstone.part

import alexiil.mc.lib.multipart.api.MultipartEventBus
import alexiil.mc.lib.multipart.api.MultipartHolder
import alexiil.mc.lib.multipart.api.PartDefinition
import alexiil.mc.lib.multipart.api.event.PartRedstonePowerEvent
import alexiil.mc.lib.multipart.api.event.PartTickEvent
import alexiil.mc.lib.net.IMsgReadCtx
import alexiil.mc.lib.net.IMsgWriteCtx
import alexiil.mc.lib.net.NetByteBuf
import com.kneelawk.wiredredstone.logic.RedstoneLogic
import com.kneelawk.wiredredstone.util.*
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.Direction
import kotlin.math.max

abstract class AbstractSimpleGatePart : AbstractGatePart {

    var outputPower: Int
        protected set
    var outputReversePower: Int
        private set

    constructor(
        definition: PartDefinition, holder: MultipartHolder, side: Direction, connections: UByte, direction: Direction,
        outputPower: Int, outputReversePower: Int
    ) : super(definition, holder, side, connections, direction) {
        this.outputPower = outputPower
        this.outputReversePower = outputReversePower
    }

    constructor(definition: PartDefinition, holder: MultipartHolder, tag: NbtCompound) : super(
        definition, holder, tag
    ) {
        outputPower = tag.getByte("outputPower").toInt().coerceIn(0..15)
        outputReversePower = tag.getByte("outputReversePower").toInt().coerceIn(0..15)
    }

    constructor(definition: PartDefinition, holder: MultipartHolder, buffer: NetByteBuf, ctx: IMsgReadCtx) : super(
        definition, holder, buffer, ctx
    ) {
        outputPower = buffer.readFixedBits(4).coerceIn(0..15)
        outputReversePower = buffer.readFixedBits(4).coerceIn(0..15)
    }

    abstract fun shouldRecalculate(): Boolean

    abstract fun recalculate()

    override fun toTag(): NbtCompound {
        val tag = super.toTag()

        tag.putByte("outputPower", outputPower.toByte())
        tag.putByte("outputReversePower", outputReversePower.toByte())

        return tag
    }

    override fun writeCreationData(buffer: NetByteBuf, ctx: IMsgWriteCtx) {
        super.writeCreationData(buffer, ctx)
        buffer.writeFixedBits(outputPower, 4)
        buffer.writeFixedBits(outputReversePower, 4)
    }

    override fun writeRenderData(buffer: NetByteBuf, ctx: IMsgWriteCtx) {
        super.writeRenderData(buffer, ctx)
        buffer.writeFixedBits(outputPower, 4)
        buffer.writeFixedBits(outputReversePower, 4)
    }

    override fun readRenderData(buffer: NetByteBuf, ctx: IMsgReadCtx) {
        super.readRenderData(buffer, ctx)
        outputPower = buffer.readFixedBits(4).coerceIn(0..15)
        outputReversePower = buffer.readFixedBits(4).coerceIn(0..15)
    }

    override fun onAdded(bus: MultipartEventBus) {
        super.onAdded(bus)

        bus.addListener(this, PartTickEvent::class.java) {
            val world = getWorld()
            if (world is ServerWorld) {
                if (shouldRecalculate()) {
                    recalculate()

                    val pos = getPos()
                    RedstoneLogic.scheduleUpdate(world, pos)
                    redraw()

                    // update neighbors
                    val edge = RotationUtils.rotatedDirection(side, direction)
                    WorldUtils.strongUpdateOutputNeighbors(world, pos, edge)
                }
            }
        }

        bus.addListener(this, PartRedstonePowerEvent::class.java) { e ->
            e.set(getRedstoneOutputPower(e.side))
        }
    }

    override fun onRemoved() {
        super.onRemoved()

        if (!isClientSide()) {
            val edge = RotationUtils.rotatedDirection(side, getOutputSide())
            WorldUtils.strongUpdateOutputNeighbors(getWorld(), getPos(), edge)
        }
    }

    private fun getRedstoneOutputPower(powerSide: Direction): Int {
        val edge = RotationUtils.rotatedDirection(side, getOutputSide())
        return if (RedstoneLogic.wiresGivePower && powerSide == edge) outputPower else 0
    }

    fun getTotalOutputPower(): Int {
        return max(outputPower, outputReversePower)
    }

    protected fun calculatePortPower(portSide: Direction): Int {
        val edge = RotationUtils.rotatedDirection(side, portSide)
        return getWorld().getEmittedRedstonePower(getPos().offset(edge), edge)
    }

    fun calculateOutputReversePower(): Int {
        return calculatePortPower(getOutputSide())
    }

    /**
     * Gets the cardinal direction of the output side.
     */
    fun getOutputSide(): Direction {
        return direction
    }

    open fun updateOutputReversePower(power: Int) {
        val changed = this.outputReversePower != power
        this.outputReversePower = power

        if (changed) {
            redraw()
            getBlockEntity().markDirty()
        }
    }
}
