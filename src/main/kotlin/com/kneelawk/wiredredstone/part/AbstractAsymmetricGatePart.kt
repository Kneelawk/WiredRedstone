package com.kneelawk.wiredredstone.part

import alexiil.mc.lib.multipart.api.MultipartEventBus
import alexiil.mc.lib.multipart.api.MultipartHolder
import alexiil.mc.lib.multipart.api.PartDefinition
import alexiil.mc.lib.multipart.api.event.PartTransformEvent
import alexiil.mc.lib.net.IMsgReadCtx
import alexiil.mc.lib.net.IMsgWriteCtx
import alexiil.mc.lib.net.NetByteBuf
import com.kneelawk.wiredredstone.util.isClientSide
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.Direction

abstract class AbstractAsymmetricGatePart : AbstractGatePart {

    var flipped: Boolean
        private set

    constructor(
        definition: PartDefinition, holder: MultipartHolder, side: Direction, connections: UByte, direction: Direction,
        flipped: Boolean
    ) : super(definition, holder, side, connections, direction) {
        this.flipped = flipped
    }

    constructor(definition: PartDefinition, holder: MultipartHolder, tag: NbtCompound) : super(
        definition, holder, tag
    ) {
        flipped = tag.getBoolean("flipped")
    }

    constructor(definition: PartDefinition, holder: MultipartHolder, buffer: NetByteBuf, ctx: IMsgReadCtx) : super(
        definition, holder, buffer, ctx
    ) {
        flipped = buffer.readBoolean()
    }

    override fun toTag(): NbtCompound {
        val tag = super.toTag()
        tag.putBoolean("flipped", flipped)
        return tag
    }

    override fun writeCreationData(buffer: NetByteBuf, ctx: IMsgWriteCtx) {
        super.writeCreationData(buffer, ctx)
        buffer.writeBoolean(flipped)
    }

    override fun writeRenderData(buffer: NetByteBuf, ctx: IMsgWriteCtx) {
        super.writeRenderData(buffer, ctx)
        buffer.writeBoolean(flipped)
    }

    override fun readRenderData(buffer: NetByteBuf, ctx: IMsgReadCtx) {
        super.readRenderData(buffer, ctx)
        flipped = buffer.readBoolean()
    }

    override fun onAdded(bus: MultipartEventBus) {
        super.onAdded(bus)

        if (isClientSide()) return

        bus.addListener(this, PartTransformEvent::class.java) { e ->
            val trans = e.transformation
            if (trans.shouldFlipDirection(Direction.Axis.X)
                xor trans.shouldFlipDirection(Direction.Axis.Y)
                xor trans.shouldFlipDirection(Direction.Axis.Z)
            ) {
                flipped = !flipped
            }
        }
    }

    override fun onMirror(axis: Direction.Axis) {
        super.onMirror(axis)
        flipped = !flipped
    }
}
