package com.kneelawk.wiredredstone.part

import alexiil.mc.lib.multipart.api.MultipartHolder
import alexiil.mc.lib.multipart.api.PartDefinition
import alexiil.mc.lib.net.IMsgReadCtx
import alexiil.mc.lib.net.IMsgWriteCtx
import alexiil.mc.lib.net.NetByteBuf
import com.kneelawk.wiredredstone.util.DirectionUtils
import com.kneelawk.wiredredstone.util.SidedOrientation
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.Direction

abstract class AbstractRotatedPart : AbstractConnectablePart, RotatedPart {
    var direction: Direction
        private set

    val orientation: SidedOrientation
        get() = SidedOrientation(side, direction)

    constructor(
        definition: PartDefinition, holder: MultipartHolder, side: Direction, connections: UByte, direction: Direction
    ) : super(definition, holder, side, connections) {
        DirectionUtils.assertHorizontal(direction)
        this.direction = direction
    }

    constructor(definition: PartDefinition, holder: MultipartHolder, tag: NbtCompound) : super(
        definition, holder, tag
    ) {
        direction = DirectionUtils.HORIZONTALS[tag.getByte("direction").toInt().coerceIn(0..3)]
    }

    constructor(definition: PartDefinition, holder: MultipartHolder, buffer: NetByteBuf, ctx: IMsgReadCtx) : super(
        definition, holder, buffer, ctx
    ) {
        direction = DirectionUtils.HORIZONTALS[buffer.readFixedBits(2).coerceIn(0..3)]
    }

    override fun toTag(): NbtCompound {
        val tag = super.toTag()
        tag.putByte("direction", direction.horizontal.toByte())
        return tag
    }

    override fun writeCreationData(buffer: NetByteBuf, ctx: IMsgWriteCtx) {
        super.writeCreationData(buffer, ctx)
        buffer.writeFixedBits(direction.horizontal, 2)
    }

    override fun writeRenderData(buffer: NetByteBuf, ctx: IMsgWriteCtx) {
        super.writeRenderData(buffer, ctx)
        buffer.writeFixedBits(direction.horizontal, 2)
    }

    override fun readRenderData(buffer: NetByteBuf, ctx: IMsgReadCtx) {
        super.readRenderData(buffer, ctx)
        direction = DirectionUtils.HORIZONTALS[buffer.readFixedBits(2).coerceIn(0..3)]
    }
}
