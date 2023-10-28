package com.kneelawk.wiredredstone.part

import alexiil.mc.lib.multipart.api.MultipartHolder
import alexiil.mc.lib.multipart.api.PartDefinition
import alexiil.mc.lib.net.IMsgReadCtx
import alexiil.mc.lib.net.IMsgWriteCtx
import alexiil.mc.lib.net.NetByteBuf
import com.kneelawk.wiredredstone.util.RotationUtils
import com.kneelawk.wiredredstone.util.getBlockEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.Direction

abstract class AbstractTwoInputGatePart : AbstractSimpleGatePart {

    var inputRightPower: Int
        private set
    var inputLeftPower: Int
        private set

    constructor(
        definition: PartDefinition, holder: MultipartHolder, side: Direction, connections: UByte, direction: Direction,
        inputRightPower: Int, inputBackPower: Int, inputLeftPower: Int, outputPower: Int, outputReversePower: Int
    ) : super(definition, holder, side, connections, direction, outputPower, outputReversePower) {
        this.inputRightPower = inputRightPower
        this.inputLeftPower = inputLeftPower
    }

    constructor(definition: PartDefinition, holder: MultipartHolder, tag: NbtCompound) : super(
        definition, holder, tag
    ) {
        inputRightPower = tag.getByte("inputRightPower").toInt().coerceIn(0..15)
        inputLeftPower = tag.getByte("inputLeftPower").toInt().coerceIn(0..15)
    }

    constructor(definition: PartDefinition, holder: MultipartHolder, buffer: NetByteBuf, ctx: IMsgReadCtx) : super(
        definition, holder, buffer, ctx
    ) {
        inputRightPower = buffer.readFixedBits(4).coerceIn(0..15)
        inputLeftPower = buffer.readFixedBits(4).coerceIn(0..15)
    }

    override fun toTag(): NbtCompound {
        val tag = super.toTag()

        tag.putByte("inputRightPower", inputRightPower.toByte())
        tag.putByte("inputLeftPower", inputLeftPower.toByte())

        return tag
    }

    override fun writeCreationData(buffer: NetByteBuf, ctx: IMsgWriteCtx) {
        super.writeCreationData(buffer, ctx)
        buffer.writeFixedBits(inputRightPower, 4)
        buffer.writeFixedBits(inputLeftPower, 4)
    }

    override fun writeRenderData(buffer: NetByteBuf, ctx: IMsgWriteCtx) {
        super.writeRenderData(buffer, ctx)
        buffer.writeFixedBits(inputRightPower, 4)
        buffer.writeFixedBits(inputLeftPower, 4)
    }

    override fun readRenderData(buffer: NetByteBuf, ctx: IMsgReadCtx) {
        super.readRenderData(buffer, ctx)
        inputRightPower = buffer.readFixedBits(4).coerceIn(0..15)
        inputLeftPower = buffer.readFixedBits(4).coerceIn(0..15)
    }

    override fun shouldScheduleUpdate(): Boolean {
        return calculateInputPower(InputType.RIGHT) != inputRightPower
                || calculateInputPower(InputType.LEFT) != inputLeftPower
                || calculateOutputReversePower() != outputReversePower
    }

    fun calculateInputPower(type: InputType): Int {
        return calculatePortPower(getInputSide(type))
    }

    /**
     * Gets the cardinal direction of the corresponding input side.
     */
    fun getInputSide(type: InputType): Direction {
        return RotationUtils.cardinalRotatedDirection(direction, type.cardinal)
    }

    open fun updateInputPower(power: Int, type: InputType) {
        val changed = when (type) {
            InputType.RIGHT -> {
                val changed = inputRightPower != power
                inputRightPower = power
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
        LEFT(Direction.WEST)
    }
}
