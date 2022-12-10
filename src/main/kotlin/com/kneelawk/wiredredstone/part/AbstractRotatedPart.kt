package com.kneelawk.wiredredstone.part

import alexiil.mc.lib.multipart.api.MultipartEventBus
import alexiil.mc.lib.multipart.api.MultipartHolder
import alexiil.mc.lib.multipart.api.PartDefinition
import alexiil.mc.lib.multipart.api.event.PartPreTransformEvent
import alexiil.mc.lib.multipart.api.event.PartTransformEvent
import alexiil.mc.lib.net.IMsgReadCtx
import alexiil.mc.lib.net.IMsgWriteCtx
import alexiil.mc.lib.net.NetByteBuf
import com.kneelawk.wiredredstone.util.DirectionUtils
import com.kneelawk.wiredredstone.util.RotationUtils
import com.kneelawk.wiredredstone.util.SidedOrientation
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.Direction

abstract class AbstractRotatedPart : AbstractConnectablePart, RotatedPart {
    var direction: Direction
        private set

    val orientation: SidedOrientation
        get() = SidedOrientation(side, direction)

    private var oldSide = side

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

    override fun onAdded(bus: MultipartEventBus) {
        super.onAdded(bus)

        bus.addContextlessListener(this, PartPreTransformEvent::class.java) {
            oldSide = side
        }

        bus.addListener(this, PartTransformEvent::class.java) { e ->
            val oldDirection = direction
            val old = RotationUtils.rotatedDirection(oldSide, oldDirection)
            val newSide = e.transformation.map(oldSide)
            val new = e.transformation.map(old)
            direction = RotationUtils.unrotatedDirection(newSide, new)

            assert(DirectionUtils.isHorizontal(direction)) {
                "Ended up with non-planar direction after rotation!\n" +
                        "Transformation: ${e.transformation}\n" +
                        "Old Direction : $oldDirection\n" +
                        "Old Un-Rotated: $old\n" +
                        "Old Side      : $oldSide\n" +
                        "New Side      : $newSide\n" +
                        "New Un-Rotated: $new\n" +
                        "New Direction : $direction"
            }
        }
    }
}
