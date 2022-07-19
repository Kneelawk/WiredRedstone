package com.kneelawk.wiredredstone.part

import alexiil.mc.lib.multipart.api.MultipartEventBus
import alexiil.mc.lib.multipart.api.MultipartHolder
import alexiil.mc.lib.multipart.api.PartDefinition
import alexiil.mc.lib.multipart.api.event.PartRedstonePowerEvent
import alexiil.mc.lib.multipart.api.event.PartTickEvent
import alexiil.mc.lib.net.IMsgReadCtx
import alexiil.mc.lib.net.IMsgWriteCtx
import alexiil.mc.lib.net.NetByteBuf
import com.kneelawk.wiredredstone.util.*
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.Direction
import kotlin.math.max

abstract class AbstractThreeInputGatePart : AbstractGatePart {

    var inputRightPower: Int
        private set
    var inputBackPower: Int
        private set
    var inputLeftPower: Int
        private set
    var outputPower: Int
        protected set
    var outputReversePower: Int
        private set

    constructor(
        definition: PartDefinition, holder: MultipartHolder, side: Direction, connections: UByte, direction: Direction,
        inputRightPower: Int, inputBackPower: Int, inputLeftPower: Int, outputPower: Int, outputReversePower: Int
    ) : super(definition, holder, side, connections, direction) {
        this.inputRightPower = inputRightPower
        this.inputBackPower = inputBackPower
        this.inputLeftPower = inputLeftPower
        this.outputPower = outputPower
        this.outputReversePower = outputReversePower
    }

    constructor(definition: PartDefinition, holder: MultipartHolder, tag: NbtCompound) : super(
        definition, holder, tag
    ) {
        inputRightPower = tag.getByte("inputRightPower").toInt().coerceIn(0..15)
        inputBackPower = tag.getByte("inputBackPower").toInt().coerceIn(0..15)
        inputLeftPower = tag.getByte("inputLeftPower").toInt().coerceIn(0..15)
        outputPower = tag.getByte("outputPower").toInt().coerceIn(0..15)
        outputReversePower = tag.getByte("outputReversePower").toInt().coerceIn(0..15)
    }

    constructor(definition: PartDefinition, holder: MultipartHolder, buffer: NetByteBuf, ctx: IMsgReadCtx) : super(
        definition, holder, buffer, ctx
    ) {
        inputRightPower = buffer.readFixedBits(4).coerceIn(0..15)
        inputBackPower = buffer.readFixedBits(4).coerceIn(0..15)
        inputLeftPower = buffer.readFixedBits(4).coerceIn(0..15)
        outputPower = buffer.readFixedBits(4).coerceIn(0..15)
        outputReversePower = buffer.readFixedBits(4).coerceIn(0..15)
    }

    abstract fun shouldRecalculate(): Boolean

    abstract fun recalculate()

    override fun toTag(): NbtCompound {
        val tag = super.toTag()

        tag.putByte("inputRightPower", inputRightPower.toByte())
        tag.putByte("inputBackPower", inputBackPower.toByte())
        tag.putByte("inputLeftPower", inputLeftPower.toByte())
        tag.putByte("outputPower", outputPower.toByte())
        tag.putByte("outputReversePower", outputReversePower.toByte())

        return tag
    }

    override fun writeCreationData(buffer: NetByteBuf, ctx: IMsgWriteCtx) {
        super.writeCreationData(buffer, ctx)
        buffer.writeFixedBits(inputRightPower, 4)
        buffer.writeFixedBits(inputBackPower, 4)
        buffer.writeFixedBits(inputLeftPower, 4)
        buffer.writeFixedBits(outputPower, 4)
        buffer.writeFixedBits(outputReversePower, 4)
    }

    override fun writeRenderData(buffer: NetByteBuf, ctx: IMsgWriteCtx) {
        super.writeRenderData(buffer, ctx)
        buffer.writeFixedBits(inputRightPower, 4)
        buffer.writeFixedBits(inputBackPower, 4)
        buffer.writeFixedBits(inputLeftPower, 4)
        buffer.writeFixedBits(outputPower, 4)
        buffer.writeFixedBits(outputReversePower, 4)
    }

    override fun readRenderData(buffer: NetByteBuf, ctx: IMsgReadCtx) {
        super.readRenderData(buffer, ctx)
        inputRightPower = buffer.readFixedBits(4).coerceIn(0..15)
        inputBackPower = buffer.readFixedBits(4).coerceIn(0..15)
        inputLeftPower = buffer.readFixedBits(4).coerceIn(0..15)
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
            val edge = RotationUtils.rotatedDirection(side, direction)
            WorldUtils.strongUpdateOutputNeighbors(getWorld(), getPos(), edge)
        }
    }

    private fun getRedstoneOutputPower(powerSide: Direction): Int {
        val edge = RotationUtils.rotatedDirection(side, direction)
        return if (RedstoneLogic.wiresGivePower && powerSide == edge) outputPower else 0
    }

    override fun shouldScheduleUpdate(): Boolean {
        return calculateInputPower(InputType.RIGHT) != inputRightPower
                || calculateInputPower(InputType.BACK) != inputBackPower
                || calculateInputPower(InputType.LEFT) != inputLeftPower
                || calculateOutputReversePower() != outputReversePower
    }

    fun getTotalOutputPower(): Int {
        return max(outputPower, outputReversePower)
    }

    private fun calculatePortPower(portSide: Direction): Int {
        val edge = RotationUtils.rotatedDirection(side, portSide)
        return getWorld().getEmittedRedstonePower(getPos().offset(edge), edge)
    }

    fun calculateInputPower(type: InputType): Int {
        return calculatePortPower(getInputSide(type))
    }

    fun calculateOutputReversePower(): Int {
        return calculatePortPower(getOutputSide())
    }

    /**
     * Gets the cardinal direction of the corresponding input side.
     */
    fun getInputSide(type: InputType): Direction {
        return RotationUtils.cardinalRotatedDirection(direction, type.cardinal)
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

    open fun updateInputPower(power: Int, type: InputType) {
        val changed = when (type) {
            InputType.RIGHT -> {
                val changed = inputRightPower != power
                inputRightPower = power
                changed
            }
            InputType.BACK -> {
                val changed = inputBackPower != power
                inputBackPower = power
                changed
            }
            InputType.LEFT -> {
                val changed = inputLeftPower != power
                inputLeftPower = power
                changed
            }
        }

        if (changed) {
            redraw()
            getBlockEntity().markDirty()
        }
    }

    enum class InputType(val cardinal: Direction) {
        RIGHT(Direction.EAST),
        BACK(Direction.SOUTH),
        LEFT(Direction.WEST)
    }
}
