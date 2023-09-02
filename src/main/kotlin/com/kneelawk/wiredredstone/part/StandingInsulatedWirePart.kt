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
import com.kneelawk.wiredredstone.item.WRItems
import com.kneelawk.wiredredstone.logic.RedstoneLogic
import com.kneelawk.wiredredstone.node.StandingInsulatedWireBlockNode
import com.kneelawk.wiredredstone.part.key.StandingInsulatedWirePartKey
import com.kneelawk.wiredredstone.util.*
import com.kneelawk.wiredredstone.util.bits.CenterConnectionUtils
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.loot.context.LootContextParameterSet
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.DyeColor
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape

class StandingInsulatedWirePart : AbstractCenterRedstoneWirePart {
    companion object {
        const val WIRE_DIAMETER = 4.0

        private val CONFLICT_SHAPE = PixelBox(
            8.0 - WIRE_DIAMETER / 2.0, 8.0 - WIRE_DIAMETER / 2.0, 8.0 - WIRE_DIAMETER / 2.0, 8.0 + WIRE_DIAMETER / 2.0,
            8.0 + WIRE_DIAMETER / 2.0, 8.0 + WIRE_DIAMETER / 2.0
        ).vs()
        private val OUTLINE_SHAPES = BoundingBoxUtils.getCenterWireOutlineShapes(WIRE_DIAMETER + 2.0)
        private val COLLISION_SHAPES = BoundingBoxUtils.getCenterWireOutlineShapes(WIRE_DIAMETER)
    }

    val color: DyeColor

    override val wireDiameter = WIRE_DIAMETER

    constructor(
        definition: PartDefinition, holder: MultipartHolder, connections: UByte, blockage: UByte, power: Int,
        color: DyeColor
    ) : super(definition, holder, connections, blockage, power) {
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

    private fun getRedstonePower(side: Direction): Int {
        return if (RedstoneLogic.wiresGivePower
            && CenterConnectionUtils.test(connections, side)
            && !CenterConnectionUtils.test(blockage, side)
        ) power else 0
    }

    override fun getShape(): VoxelShape {
        return CONFLICT_SHAPE
    }

    override fun getCollisionShape(): VoxelShape {
        return COLLISION_SHAPES[connections]
    }

    override fun getOutlineShape(): VoxelShape {
        return OUTLINE_SHAPES[connections]
    }

    override fun getModelKey(): PartModelKey? {
        return StandingInsulatedWirePartKey(color, connections, power != 0)
    }

    override fun createBlockNodes(): Collection<BlockNode> {
        return listOf(StandingInsulatedWireBlockNode(color))
    }

    override fun getClosestBlockState(): BlockState {
        return DyeColorUtil.wool(color).defaultState
    }

    override fun getPickStack(hitResult: BlockHitResult?): ItemStack {
        return ItemStack(WRItems.STANDING_INSULATED_WIRES[color]!!)
    }

    override fun addDrops(target: ItemDropTarget, params: LootContextParameterSet) {
        val identifier = WRParts.STANDING_INSULATED_WIRE.identifier.withPrefix(color.getName() + "_")
        LootTableUtil.addPartDrops(this, target, params, identifier)
    }
}
