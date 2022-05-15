package com.kneelawk.wiredredstone.part

import alexiil.mc.lib.multipart.api.MultipartEventBus
import alexiil.mc.lib.multipart.api.MultipartHolder
import alexiil.mc.lib.multipart.api.PartDefinition
import alexiil.mc.lib.multipart.api.event.PartRedstonePowerEvent
import alexiil.mc.lib.multipart.api.event.PartTickEvent
import alexiil.mc.lib.multipart.api.property.MultipartProperties
import alexiil.mc.lib.net.IMsgReadCtx
import alexiil.mc.lib.net.IMsgWriteCtx
import alexiil.mc.lib.net.NetByteBuf
import com.kneelawk.wiredredstone.util.*
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.Direction
import kotlin.math.max

abstract class AbstractInputOutputGatePart : AbstractGatePart {

    var inputPower: Int
        private set
    var outputPower: Int
        protected set
    var outputReversePower: Int
        private set

    constructor(
        definition: PartDefinition, holder: MultipartHolder, side: Direction, connections: UByte, direction: Direction,
        inputPower: Int, outputPower: Int, outputReversePower: Int
    ) : super(definition, holder, side, connections, direction) {
        this.inputPower = inputPower
        this.outputPower = outputPower
        this.outputReversePower = outputReversePower
    }

    constructor(definition: PartDefinition, holder: MultipartHolder, tag: NbtCompound) : super(
        definition, holder, tag
    ) {
        inputPower = tag.getByte("inputPower").toInt().coerceIn(0..15)
        outputPower = tag.getByte("outputPower").toInt().coerceIn(0..15)
        outputReversePower = tag.getByte("outputReversePower").toInt().coerceIn(0..15)
    }

    constructor(definition: PartDefinition, holder: MultipartHolder, buffer: NetByteBuf, ctx: IMsgReadCtx) : super(
        definition, holder, buffer, ctx
    ) {
        inputPower = buffer.readByte().toInt().coerceIn(0..15)
        outputPower = buffer.readByte().toInt().coerceIn(0..15)
        outputReversePower = buffer.readByte().toInt().coerceIn(0..15)
    }

    abstract fun shouldRecalculate(): Boolean

    abstract fun recalculate()

    override fun toTag(): NbtCompound {
        val tag = super.toTag()
        tag.putByte("inputPower", inputPower.toByte())
        tag.putByte("outputPower", outputPower.toByte())
        tag.putByte("outputReversePower", outputReversePower.toByte())
        return tag
    }

    override fun writeCreationData(buffer: NetByteBuf, ctx: IMsgWriteCtx) {
        super.writeCreationData(buffer, ctx)
        buffer.writeByte(inputPower)
        buffer.writeByte(outputPower)
        buffer.writeByte(outputReversePower)
    }

    override fun writeRenderData(buffer: NetByteBuf, ctx: IMsgWriteCtx) {
        super.writeRenderData(buffer, ctx)
        buffer.writeByte(inputPower)
        buffer.writeByte(outputPower)
        buffer.writeByte(outputReversePower)
    }

    override fun readRenderData(buffer: NetByteBuf, ctx: IMsgReadCtx) {
        super.readRenderData(buffer, ctx)
        inputPower = buffer.readByte().toInt().coerceIn(0..15)
        outputPower = buffer.readByte().toInt().coerceIn(0..15)
        outputReversePower = buffer.readByte().toInt().coerceIn(0..15)
    }

    override fun onAdded(bus: MultipartEventBus) {
        super.onAdded(bus)
        getProperties().setValue(this, MultipartProperties.CAN_EMIT_REDSTONE, true)

        bus.addListener(this, PartTickEvent::class.java) {
            val world = getWorld()
            if (world is ServerWorld) {
                // We do this here so that the delay between input and output is always the same
                // regardless of wirenet update order.
                if (shouldRecalculate()) {
                    recalculate()

                    val pos = getPos()
                    RedstoneLogic.scheduleUpdate(world, pos)
                    redraw()

                    // Update neighbors
                    val edge = RotationUtils.rotatedDirection(side, direction)
                    WorldUtils.strongUpdateNeighbors(world, pos, edge)
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
            val edge = RotationUtils.rotatedDirection(side, direction)
            WorldUtils.strongUpdateNeighbors(getWorld(), getPos(), edge)
        }
    }

    private fun getRedstoneOutputPower(powerSide: Direction): Int {
        val edge = RotationUtils.rotatedDirection(side, direction)
        return if (RedstoneLogic.wiresGivePower && powerSide == edge) outputPower else 0
    }

    override fun shouldScheduleUpdate(): Boolean {
        return calculateInputPower() != inputPower || calculateOutputReversePower() != outputReversePower
    }

    fun getTotalOutputPower(): Int {
        return max(outputPower, outputReversePower)
    }

    fun calculateInputPower(): Int {
        val edge = RotationUtils.rotatedDirection(side, getInputSide())
        return getWorld().getEmittedRedstonePower(getPos().offset(edge), edge)
    }

    fun calculateOutputReversePower(): Int {
        val edge = RotationUtils.rotatedDirection(side, getOutputSide())
        return getWorld().getEmittedRedstonePower(getPos().offset(edge), edge)
    }

    /**
     * Gets the cardinal direction of the input side.
     */
    fun getInputSide(): Direction {
        return direction.opposite
    }

    /**
     * Gets the cardinal direction of the output side.
     */
    fun getOutputSide(): Direction {
        return direction
    }

    fun updateOutputReversePower(power: Int) {
        this.outputReversePower = power
        redraw()
        getBlockEntity().markDirty()

//        val edge = RotationUtils.rotatedDirection(side, direction)
//        WorldUtils.strongUpdateNeighbors(getWorld(), getPos(), edge)
    }

    open fun updateInputPower(power: Int) {
        this.inputPower = power
        redraw()
        getBlockEntity().markDirty()
    }
}