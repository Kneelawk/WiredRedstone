package com.kneelawk.wiredredstone.part

import alexiil.mc.lib.multipart.api.MultipartEventBus
import alexiil.mc.lib.multipart.api.MultipartHolder
import alexiil.mc.lib.multipart.api.PartDefinition
import alexiil.mc.lib.multipart.api.event.PartRedstonePowerEvent
import alexiil.mc.lib.multipart.api.render.PartModelKey
import alexiil.mc.lib.net.IMsgReadCtx
import alexiil.mc.lib.net.IMsgWriteCtx
import alexiil.mc.lib.net.NetByteBuf
import com.kneelawk.wiredredstone.part.key.InsulatedWirePartKey
import com.kneelawk.wiredredstone.partext.InsulatedWirePartExt
import com.kneelawk.wiredredstone.util.*
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.loot.context.LootContext
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.DyeColor
import net.minecraft.util.Identifier
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape

class InsulatedWirePart : AbstractRedstoneWirePart {
    companion object {
        const val WIRE_WIDTH = 4.0
        const val WIRE_HEIGHT = 3.0

        private val CONFLICT_SHAPES = BoundingBoxUtils.getWireConflictShapes(WIRE_WIDTH, WIRE_HEIGHT)
        private val OUTLINE_SHAPES = BoundingBoxUtils.getWireOutlineShapes(10.0, WIRE_HEIGHT)
    }

    val color: DyeColor

    override val partExtType = InsulatedWirePartExt.Type
    override val wireWidth = WIRE_WIDTH
    override val wireHeight = WIRE_HEIGHT

    constructor(
        definition: PartDefinition, holder: MultipartHolder, side: Direction, connections: UByte, power: Int,
        blockage: UByte, color: DyeColor
    ) : super(definition, holder, side, connections, power, blockage) {
        this.color = color
    }

    constructor(definition: PartDefinition, holder: MultipartHolder, tag: NbtCompound) : super(
        definition, holder, tag
    ) {
        color = DyeColor.byId(tag.getByte("color").toInt())
    }

    constructor(definition: PartDefinition, holder: MultipartHolder, buffer: NetByteBuf, ctx: IMsgReadCtx) : super(
        definition, holder, buffer, ctx
    ) {
        color = DyeColor.byId(buffer.readByte().toInt())
    }

    override fun toTag(): NbtCompound {
        val tag = super.toTag()
        tag.putByte("color", color.id.toByte())
        return tag
    }

    override fun writeCreationData(buffer: NetByteBuf, ctx: IMsgWriteCtx) {
        super.writeCreationData(buffer, ctx)
        buffer.writeByte(color.id)
    }

    override fun onAdded(bus: MultipartEventBus) {
        super.onAdded(bus)

        bus.addListener(this, PartRedstonePowerEvent.PartStrongRedstonePowerEvent::class.java) {
            // Fix comparator side input
            if (getWorld().getBlockState(getPos().offset(it.side)).block == Blocks.COMPARATOR) {
                it.set(getRedstonePower(it.side))
            }
        }

        bus.addListener(this, PartRedstonePowerEvent.PartWeakRedstonePowerEvent::class.java) {
            it.set(getRedstonePower(it.side))
        }
    }

    private fun getRedstonePower(powerSide: Direction): Int {
        return if (RedstoneLogic.wiresGivePower && powerSide == side) power else 0
    }

    override fun getReceivingPower(): Int {
        return RedstoneLogic.getReceivingPower(getWorld(), getSidedPos(), connections, false, blockage)
    }

    override fun getShape(): VoxelShape {
        return CONFLICT_SHAPES[side]!!
    }

    override fun getModelKey(): PartModelKey {
        return InsulatedWirePartKey(side, connections, color, power != 0)
    }

    override fun getOutlineShape(): VoxelShape {
        return OUTLINE_SHAPES[BoundingBoxUtils.ShapeKey(side, connections)]
    }

    override fun getClosestBlockState(): BlockState {
        return DyeColorUtil.wool(color).defaultState
    }

    override fun getPickStack(hitResult: BlockHitResult?): ItemStack {
        return ItemStack(DyeColorUtil.insulatedWire(color))
    }

    override fun addDrops(target: ItemDropTarget, context: LootContext) {
        val base = WRParts.INSULATED_WIRE.identifier
        val identifier = Identifier(base.namespace, "${color.getName()}_${base.path}")
        LootTableUtil.addPartDrops(getWorld(), target, context, identifier)
    }
}