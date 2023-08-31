package com.kneelawk.wiredredstone.part

import alexiil.mc.lib.multipart.api.MultipartEventBus
import alexiil.mc.lib.multipart.api.MultipartHolder
import alexiil.mc.lib.multipart.api.PartDefinition
import alexiil.mc.lib.multipart.api.event.PartRedstonePowerEvent
import alexiil.mc.lib.multipart.api.render.PartModelKey
import alexiil.mc.lib.net.IMsgReadCtx
import alexiil.mc.lib.net.IMsgWriteCtx
import alexiil.mc.lib.net.NetByteBuf
import com.kneelawk.graphlib.api.graph.user.BlockNode
import com.kneelawk.wiredredstone.logic.RedstoneLogic
import com.kneelawk.wiredredstone.node.InsulatedWireBlockNode
import com.kneelawk.wiredredstone.part.key.InsulatedWirePartKey
import com.kneelawk.wiredredstone.util.*
import com.kneelawk.wiredredstone.util.bits.BlockageUtils
import com.kneelawk.wiredredstone.util.bits.ConnectionUtils
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.loot.context.LootContextParameterSet
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

    override fun createBlockNodes(): Collection<BlockNode> {
        return listOf(InsulatedWireBlockNode(side, color))
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
        val cardinal = RotationUtils.unrotatedDirection(side, powerSide)
        return if (RedstoneLogic.wiresGivePower
            && (powerSide == side
                    || (DirectionUtils.isHorizontal(cardinal)
                    && !ConnectionUtils.isDisconnected(connections, cardinal)
                    && !BlockageUtils.isBlocked(blockage, cardinal)))
        ) power else 0
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

    override fun addDrops(target: ItemDropTarget, params: LootContextParameterSet) {
        val identifier = WRParts.INSULATED_WIRE.identifier.withPrefix(color.getName() + "_")
        LootTableUtil.addPartDrops(this, target, params, identifier)
    }
}
