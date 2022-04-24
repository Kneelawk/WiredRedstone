package com.kneelawk.wiredredstone.part

import alexiil.mc.lib.multipart.api.MultipartEventBus
import alexiil.mc.lib.multipart.api.MultipartHolder
import alexiil.mc.lib.multipart.api.PartDefinition
import alexiil.mc.lib.multipart.api.event.NeighbourUpdateEvent
import alexiil.mc.lib.multipart.api.event.PartAddedEvent
import alexiil.mc.lib.multipart.api.event.PartRemovedEvent
import alexiil.mc.lib.multipart.api.render.PartModelKey
import alexiil.mc.lib.net.IMsgReadCtx
import alexiil.mc.lib.net.IMsgWriteCtx
import alexiil.mc.lib.net.NetByteBuf
import com.kneelawk.wiredredstone.part.key.BundledCablePartKey
import com.kneelawk.wiredredstone.partext.BundledCablePartExt
import com.kneelawk.wiredredstone.util.*
import com.kneelawk.wiredredstone.wirenet.NetNodeContainer
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.loot.context.LootContext
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.DyeColor
import net.minecraft.util.Identifier
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes

class BundledCablePart : AbstractBlockablePart {
    companion object {
        const val WIRE_WIDTH = 6.0
        const val WIRE_HEIGHT = 4.0

        private val CONFLICT_SHAPES = BoundingBoxUtils.getWireConflictShapes(WIRE_WIDTH, WIRE_HEIGHT)
        private val OUTLINE_SHAPES = BoundingBoxUtils.getWireOutlineShapes(12.0, WIRE_HEIGHT)
    }

    val color: DyeColor?

    override val partExtType = BundledCablePartExt.Type

    constructor(
        definition: PartDefinition, holder: MultipartHolder, side: Direction, connections: UByte, blockage: UByte,
        color: DyeColor?
    ) : super(
        definition, holder, side, connections, blockage
    ) {
        this.color = color
    }

    constructor(definition: PartDefinition, holder: MultipartHolder, tag: NbtCompound) : super(
        definition, holder, tag
    ) {
        color = if (tag.contains("color")) DyeColor.byId(tag.getByte("color").toInt()) else null
    }

    constructor(definition: PartDefinition, holder: MultipartHolder, buffer: NetByteBuf, ctx: IMsgReadCtx) : super(
        definition, holder, buffer, ctx
    ) {
        color = if (buffer.readBoolean()) DyeColor.byId(buffer.readByte().toInt()) else null
    }

    override fun toTag(): NbtCompound {
        val tag = super.toTag()
        color?.let { tag.putByte("color", it.id.toByte()) }
        return tag
    }

    override fun writeCreationData(buffer: NetByteBuf, ctx: IMsgWriteCtx) {
        super.writeCreationData(buffer, ctx)
        buffer.writeBoolean(color != null)
        color?.let { buffer.writeByte(it.id) }
    }

    override fun onAdded(bus: MultipartEventBus) {
        super.onAdded(bus)

        bus.addListener(this, NeighbourUpdateEvent::class.java) {
            // Sometimes this gets called after this part has been removed already
            if (isRemoved()) return@addListener

            handleUpdates()
        }

        bus.addListener(this, PartAddedEvent::class.java) { e ->
            // NetNodeContainers update our connections directly when changed
            if (e.part !is NetNodeContainer) {
                handleUpdates()
            }
        }

        bus.addListener(this, PartRemovedEvent::class.java) { e ->
            // NetNodeContainers update our connections directly when changed
            if (e.removed !is NetNodeContainer) {
                handleUpdates()
            }
        }
    }

    fun handleUpdates() {
        val world = getWorld()
        if (world is ServerWorld) {
            ConnectableUtils.updateBlockageAndConnections(world, this, WIRE_WIDTH, WIRE_HEIGHT)
            RedstoneLogic.scheduleUpdate(world, getPos())
        }
    }

    override fun getShape(): VoxelShape {
        return CONFLICT_SHAPES[side]!!
    }

    override fun getModelKey(): PartModelKey {
        return BundledCablePartKey(side, connections, color)
    }

    override fun getOutlineShape(): VoxelShape {
        return OUTLINE_SHAPES[BoundingBoxUtils.ShapeKey(side, connections)]
    }

    override fun getCollisionShape(): VoxelShape {
        return VoxelShapes.empty()
    }

    override fun getCullingShape(): VoxelShape {
        return VoxelShapes.empty()
    }

    override fun getClosestBlockState(): BlockState {
        return (color?.let(DyeColorUtil::wool) ?: Blocks.STONE).defaultState
    }

    override fun calculateBreakingDelta(player: PlayerEntity): Float {
        // Break wires instantly like redstone wire
        return super.calculateBreakingDelta(player, Blocks.REDSTONE_WIRE)
    }

    override fun getPickStack(hitResult: BlockHitResult?): ItemStack {
        return ItemStack(DyeColorUtil.bundledCable(color))
    }

    override fun addDrops(target: ItemDropTarget, context: LootContext) {
        val base = WRParts.BUNDLED_CABLE.identifier
        val identifier = color?.let { Identifier(base.namespace, "${it.getName()}_${base.path}") } ?: base
        LootTableUtil.addPartDrops(getWorld(), target, context, identifier)
    }
}
