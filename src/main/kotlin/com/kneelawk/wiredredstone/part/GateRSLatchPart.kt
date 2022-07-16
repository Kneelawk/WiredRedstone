package com.kneelawk.wiredredstone.part

import alexiil.mc.lib.multipart.api.MultipartEventBus
import alexiil.mc.lib.multipart.api.MultipartHolder
import alexiil.mc.lib.multipart.api.PartDefinition
import alexiil.mc.lib.multipart.api.event.PartRedstonePowerEvent
import alexiil.mc.lib.multipart.api.event.PartTickEvent
import alexiil.mc.lib.multipart.api.render.PartModelKey
import alexiil.mc.lib.net.IMsgReadCtx
import alexiil.mc.lib.net.IMsgWriteCtx
import alexiil.mc.lib.net.NetByteBuf
import com.kneelawk.graphlib.graph.BlockNode
import com.kneelawk.wiredredstone.node.GateRSLatchBlockNode
import com.kneelawk.wiredredstone.util.*
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Direction.*
import kotlin.math.max

class GateRSLatchPart : AbstractGatePart {

    var latchState: LatchState
        private set
    var inputSetPower: Int
        private set
    var inputResetPower: Int
        private set
    var outputSetReversePower: Int
        private set
    var outputResetReversePower: Int
        private set

    constructor(
        definition: PartDefinition, holder: MultipartHolder, side: Direction, connections: UByte, direction: Direction,
        latchState: LatchState, inputSetPower: Int, inputResetPower: Int, outputSetReversePower: Int,
        outputResetReversePower: Int
    ) : super(definition, holder, side, connections, direction) {
        this.latchState = latchState
        this.inputSetPower = inputSetPower
        this.inputResetPower = inputResetPower
        this.outputSetReversePower = outputSetReversePower
        this.outputResetReversePower = outputResetReversePower
    }

    constructor(definition: PartDefinition, holder: MultipartHolder, tag: NbtCompound) : super(
        definition, holder, tag
    ) {
        latchState = tag.getByte("latchState").toEnum()
        inputSetPower = tag.getByte("inputSetPower").toInt().coerceIn(0..15)
        inputResetPower = tag.getByte("inputResetPower").toInt().coerceIn(0..15)
        outputSetReversePower = tag.getByte("outputSetReversePower").toInt().coerceIn(0..15)
        outputResetReversePower = tag.getByte("outputResetReversePower").toInt().coerceIn(0..15)
    }

    constructor(definition: PartDefinition, holder: MultipartHolder, buffer: NetByteBuf, ctx: IMsgReadCtx) : super(
        definition, holder, buffer, ctx
    ) {
        latchState = buffer.readFixedBits(1).toEnum()
        inputSetPower = buffer.readFixedBits(4)
        inputResetPower = buffer.readFixedBits(4)
        outputSetReversePower = buffer.readFixedBits(4)
        outputResetReversePower = buffer.readFixedBits(4)
    }

    override fun toTag(): NbtCompound {
        val tag = super.toTag()
        tag.putByte("latchState", latchState.toByte())
        tag.putByte("inputSetPower", inputSetPower.toByte())
        tag.putByte("inputResetPower", inputResetPower.toByte())
        tag.putByte("outputSetReversePower", outputSetReversePower.toByte())
        tag.putByte("outputResetReversePower", outputResetReversePower.toByte())
        return tag
    }

    override fun writeCreationData(buffer: NetByteBuf, ctx: IMsgWriteCtx) {
        super.writeCreationData(buffer, ctx)
        buffer.writeFixedBits(latchState.toInt(), 1)
        buffer.writeFixedBits(inputSetPower, 4)
        buffer.writeFixedBits(inputResetPower, 4)
        buffer.writeFixedBits(outputSetReversePower, 4)
        buffer.writeFixedBits(outputResetReversePower, 4)
    }

    override fun writeRenderData(buffer: NetByteBuf, ctx: IMsgWriteCtx) {
        super.writeRenderData(buffer, ctx)
        buffer.writeFixedBits(latchState.toInt(), 1)
        buffer.writeFixedBits(inputSetPower, 4)
        buffer.writeFixedBits(inputResetPower, 4)
        buffer.writeFixedBits(outputSetReversePower, 4)
        buffer.writeFixedBits(outputResetReversePower, 4)
    }

    override fun readRenderData(buffer: NetByteBuf, ctx: IMsgReadCtx) {
        super.readRenderData(buffer, ctx)
        latchState = buffer.readFixedBits(1).toEnum()
        inputSetPower = buffer.readFixedBits(4)
        inputResetPower = buffer.readFixedBits(4)
        outputSetReversePower = buffer.readFixedBits(4)
        outputResetReversePower = buffer.readFixedBits(4)
    }

    override fun onAdded(bus: MultipartEventBus) {
        super.onAdded(bus)

        bus.addListener(this, PartTickEvent::class.java) {
            val world = getWorld()
            if (world is ServerWorld) {
                // TODO: recalculate
            }
        }

        bus.addListener(this, PartRedstonePowerEvent::class.java) {
            it.set(getRedstoneOutputPower(it.side))
        }
    }

    override fun onRemoved() {
        super.onRemoved()

        if (!isClientSide()) {
            WorldUtils.strongUpdateOutputNeighbors(getWorld(), getPos(), getOutputEdge(LatchState.SET))
            WorldUtils.strongUpdateOutputNeighbors(getWorld(), getPos(), getOutputEdge(LatchState.RESET))
        }
    }

    override fun createBlockNodes(): Collection<BlockNode> {
        return listOf(
            GateRSLatchBlockNode.Input(side, LatchState.SET),
            GateRSLatchBlockNode.Input(side, LatchState.RESET),
            GateRSLatchBlockNode.Output(side, LatchState.SET),
            GateRSLatchBlockNode.Output(side, LatchState.RESET)
        )
    }

    override fun shouldScheduleUpdate(): Boolean {
        return calculateInputPower(LatchState.SET) != inputSetPower ||
                calculateInputPower(LatchState.RESET) != inputResetPower ||
                calculateOutputReversePower(LatchState.SET) != outputSetReversePower ||
                calculateOutputReversePower(LatchState.RESET) != outputResetReversePower
    }

    override fun getModelKey(): PartModelKey? {
        TODO("Not yet implemented")
    }

    private fun getRedstoneOutputPower(edge: Direction): Int {
        val poweredEdge = getOutputEdge(latchState)
        return if (RedstoneLogic.wiresGivePower && poweredEdge == edge && isOutputEnabled()) 15 else 0
    }

    private fun calculatePortPower(portSide: Direction): Int {
        val edge = RotationUtils.rotatedDirection(side, portSide)
        return getWorld().getEmittedRedstonePower(getPos().offset(edge), edge)
    }

    private fun isOutputEnabled() = inputSetPower == 0 || inputResetPower == 0

    fun calculateInputPower(state: LatchState): Int = calculatePortPower(getInputSide(state))

    fun calculateOutputReversePower(state: LatchState): Int = calculatePortPower(getOutputSide(state))

    fun getInputSide(state: LatchState): Direction =
        RotationUtils.cardinalRotatedDirection(direction, state.inputCardinal)

    fun getOutputSide(state: LatchState): Direction =
        RotationUtils.cardinalRotatedDirection(direction, state.outputCardinal)

    private fun getOutputEdge(state: LatchState): Direction =
        RotationUtils.rotatedDirection(this.side, getOutputSide(state))

    fun getOutputPower(state: LatchState): Int {
        return if (state == latchState && isOutputEnabled()) 15 else 0
    }

    fun getTotalOutputPower(state: LatchState): Int {
        return max(
            getOutputPower(state),
            when (state) {
                LatchState.SET -> outputSetReversePower
                LatchState.RESET -> outputResetReversePower
            }
        )
    }

    fun updateInputPower(power: Int, state: LatchState) {
        val changed: Boolean

        when (state) {
            LatchState.SET -> {
                changed = inputSetPower != power
                inputSetPower = power
            }
            LatchState.RESET -> {
                changed = inputResetPower != power
                inputResetPower = power
            }
        }

        if (changed) {
            redraw()
            getBlockEntity().markDirty()
        }
    }

    fun updateReverseOuputPower(power: Int, state: LatchState) {
        val changed: Boolean

        when (state) {
            LatchState.SET -> {
                changed = outputSetReversePower != power
                outputSetReversePower = power
            }
            LatchState.RESET -> {
                changed = outputResetReversePower != power
                outputResetReversePower = power
            }
        }

        if (changed) {
            redraw()
            getBlockEntity().markDirty()
        }
    }

    enum class LatchState(val inputCardinal: Direction, val outputCardinal: Direction) {
        SET(EAST, NORTH), RESET(WEST, SOUTH)
    }
}
