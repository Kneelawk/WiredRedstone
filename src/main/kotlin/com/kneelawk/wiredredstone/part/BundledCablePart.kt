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
import com.kneelawk.graphlib.graph.BlockNode
import com.kneelawk.wiredredstone.node.BundledCableBlockNode
import com.kneelawk.wiredredstone.part.key.BundledCablePartKey
import com.kneelawk.wiredredstone.util.*
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

class BundledCablePart : AbstractBlockablePart, BundledPowerablePart {
    companion object {
        const val WIRE_WIDTH = 6.0
        const val WIRE_HEIGHT = 4.0

        private val CONFLICT_SHAPES = BoundingBoxUtils.getWireConflictShapes(WIRE_WIDTH, WIRE_HEIGHT)
        private val OUTLINE_SHAPES = BoundingBoxUtils.getWireOutlineShapes(12.0, WIRE_HEIGHT)
    }

    val color: DyeColor?

    override var power: ULong
        private set

    constructor(
        definition: PartDefinition, holder: MultipartHolder, side: Direction, connections: UByte, blockage: UByte,
        color: DyeColor?, power: ULong
    ) : super(
        definition, holder, side, connections, blockage
    ) {
        this.color = color
        this.power = power
    }

    constructor(definition: PartDefinition, holder: MultipartHolder, tag: NbtCompound) : super(
        definition, holder, tag
    ) {
        color = if (tag.contains("color")) DyeColor.byId(tag.getByte("color").toInt()) else null
        power = if (tag.contains("power")) tag.getLong("power").toULong() else 0u
    }

    constructor(definition: PartDefinition, holder: MultipartHolder, buffer: NetByteBuf, ctx: IMsgReadCtx) : super(
        definition, holder, buffer, ctx
    ) {
        color = if (buffer.readBoolean()) DyeColor.byId(buffer.readByte().toInt()) else null
        // no need for power levels on the client for now
        power = 0u
    }

    override fun createBlockNodes(): Collection<BlockNode> {
        return DyeColor.values().asSequence().map { BundledCableBlockNode(side, color, it) }.toList()
    }

    override fun toTag(): NbtCompound {
        val tag = super.toTag()
        color?.let { tag.putByte("color", it.id.toByte()) }
        tag.putLong("power", power.toLong())
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
            if (e.part !is BlockNodeContainer) {
                handleUpdates()
            }
        }

        bus.addListener(this, PartRemovedEvent::class.java) { e ->
            // NetNodeContainers update our connections directly when changed
            if (e.removed !is BlockNodeContainer) {
                handleUpdates()
            }
        }
    }

    fun handleUpdates() {
        val world = getWorld()
        if (world is ServerWorld) {
            ConnectableUtils.updateBlockageAndConnections(world, this, WIRE_WIDTH, WIRE_HEIGHT)
            if (BundledCableUtils.getBundledCableInput(world, getSidedPos(), connections, blockage) != power) {
                RedstoneLogic.scheduleUpdate(world, getPos())
            }
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
        return (color?.let(DyeColorUtil::wool) ?: Blocks.WHITE_WOOL).defaultState
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

    override fun overrideConnections(connections: UByte): UByte {
        val world = getWorld()
        val pos = getPos()
        var newConn = connections

        for (cardinal in DirectionUtils.HORIZONTALS) {
            // Blockage gets updated before this gets called, so checking blockage here is ok
            if (ConnectionUtils.isDisconnected(newConn, cardinal) && !BlockageUtils.isBlocked(blockage, cardinal)) {
                val edge = RotationUtils.rotatedDirection(side, cardinal)
                val offset = pos.offset(edge)
                if (BundledCableUtils.hasBundledCableOutput(world, offset)) {
                    newConn = ConnectionUtils.setExternal(newConn, cardinal)
                }
            }
        }

        return newConn
    }

    override fun updatePower(power: ULong) {
        val changed = this.power != power
        this.power = power

        getBlockEntity().markDirty()

        if (changed) {
            WorldUtils.strongUpdateAllNeighbors(getWorld(), getPos(), side)
        }
    }

    override fun updatePower(inner: DyeColor, power: Int) {
        updatePower(BundledCableUtils.set(this.power, inner, power))
    }

    override fun getPower(inner: DyeColor): Int {
        return BundledCableUtils.get(power, inner)
    }

    override fun getPower(side: Direction): ULong {
        val cardinal = RotationUtils.unrotatedDirection(this.side, side)
        return if (RedstoneLogic.wiresGivePower
            && (side == this.side
                    || (DirectionUtils.isHorizontal(cardinal)
                    && !ConnectionUtils.isDisconnected(connections, cardinal)))
        ) power else 0uL
    }
}
