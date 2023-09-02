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
import com.kneelawk.graphlib.api.graph.user.BlockNode
import com.kneelawk.graphlib.api.util.SidedPos
import com.kneelawk.wiredredstone.item.WRItems
import com.kneelawk.wiredredstone.logic.BundledCableLogic
import com.kneelawk.wiredredstone.logic.RedstoneLogic
import com.kneelawk.wiredredstone.node.StandingBundledCableBlockNode
import com.kneelawk.wiredredstone.util.*
import com.kneelawk.wiredredstone.util.bits.CenterConnectionUtils
import com.kneelawk.wiredredstone.util.connectable.ConnectableUtils
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.loot.context.LootContextParameterSet
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.DyeColor
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape

class StandingBundledCablePart : AbstractCenterBlockablePart, BundledPowerablePart {
    companion object {
        const val WIRE_DIAMETER = 6.0

        private val CONFLICT_SHAPE = PixelBox(
            8.0 - WIRE_DIAMETER / 2.0, 8.0 - WIRE_DIAMETER / 2.0,
            8.0 - WIRE_DIAMETER / 2.0, 8.0 + WIRE_DIAMETER / 2.0,
            8.0 + WIRE_DIAMETER / 2.0, 8.0 + WIRE_DIAMETER / 2.0
        ).vs()
        private val OUTLINE_SHAPES = BoundingBoxUtils.getCenterWireOutlineShapes(
            WIRE_DIAMETER + 2.0
        )
        private val COLLISION_SHAPES = BoundingBoxUtils.getCenterWireOutlineShapes(
            WIRE_DIAMETER
        )
    }

    val color: DyeColor?

    // Used for telling if any of our neighbors should receive block updates when we change state
    private var bundledOutputs: UByte

    var power: ULong
        private set

    constructor(
        definition: PartDefinition, holder: MultipartHolder, connections: UByte, blockage: UByte, color: DyeColor?,
        bundledOutputs: UByte, power: ULong
    ) : super(
        definition, holder, connections, blockage
    ) {
        this.color = color
        this.bundledOutputs = bundledOutputs
        this.power = power
    }

    constructor(definition: PartDefinition, holder: MultipartHolder, tag: NbtCompound) : super(
        definition, holder, tag
    ) {
        color = if (tag.contains("color")) DyeColor.byId(tag.getByte("color").toInt()) else null
        bundledOutputs = tag.getByte("bundledOutputs").toUByte()
        power = tag.getLong("power").toULong()
    }

    constructor(definition: PartDefinition, holder: MultipartHolder, buffer: NetByteBuf, ctx: IMsgReadCtx) : super(
        definition, holder, buffer, ctx
    ) {
        color = if (buffer.readBoolean()) DyeColor.byId(buffer.readByte().toInt()) else null
        bundledOutputs = 0u
        power = 0u
    }

    override fun toTag(): NbtCompound {
        val tag = super.toTag()
        color?.let { tag.putByte("color", it.id.toByte()) }
        tag.putLong("power", power.toLong())
        tag.putByte("bundledOutputs", bundledOutputs.toByte())
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
            if (e.part != this) {
                handleUpdates()
            }
        }

        bus.addListener(this, PartRemovedEvent::class.java) { e ->
            if (e.removed != this) {
                handleUpdates()
            }
        }
    }

    fun handleUpdates() {
        // If this is true, we know this update was caused by another wire
        if (WorldUtils.doingUpdate) return

        val world = getWorld()
        if (world is ServerWorld) {
            ConnectableUtils.updateBlockageAndConnections(world, this, WIRE_DIAMETER)
            if (BundledCableLogic.getCenterBundledCableInput(world, getPos(), connections, blockage) != power) {
                RedstoneLogic.scheduleUpdate(world, getPos())
            }
        }
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

    override fun getClosestBlockState(): BlockState {
        return (color?.let(DyeColorUtil::wool) ?: Blocks.WHITE_WOOL).defaultState
    }

    override fun getModelKey(): PartModelKey? {
        // TODO
        return null
    }

    override fun createBlockNodes(): Collection<BlockNode> {
        return DyeColor.values().map { StandingBundledCableBlockNode(color, it) }
    }

    override fun calculateBreakingDelta(player: PlayerEntity): Float {
        // Break wires instantly like redstone wire
        return super.calculateBreakingDelta(player, Blocks.REDSTONE_WIRE)
    }

    override fun getPickStack(hitResult: BlockHitResult?): ItemStack {
        return ItemStack(WRItems.STANDING_BUNDLED_CABLES[color]!!)
    }

    override fun addDrops(target: ItemDropTarget, params: LootContextParameterSet) {
        val base = WRParts.STANDING_BUNDLED_CABLE.identifier
        val identifier = color?.let { base.withPrefix(it.getName() + "_") } ?: base
        LootTableUtil.addPartDrops(this, target, params, identifier)
    }

    override fun overrideConnections(connections: UByte): UByte {
        val world = getWorld()
        val pos = getPos()
        var newConn = connections
        var newBundledOutputs: UByte = 0u

        for (dir in Direction.values()) {
            if (!CenterConnectionUtils.test(blockage, dir)) {
                val offset = pos.offset(dir)
                if (BundledCableLogic.hasBundledCableOutput(world, SidedPos(offset, dir.opposite))) {
                    newBundledOutputs = CenterConnectionUtils.set(newBundledOutputs, dir)
                    if (!CenterConnectionUtils.test(connections, dir)) {
                        newConn = CenterConnectionUtils.set(newConn, dir)
                    }
                }
            }
        }

        bundledOutputs = newBundledOutputs

        return newConn
    }

    override fun updatePower(power: ULong) {
        val changed = this.power != power
        this.power = power

        if (changed) {
            getBlockEntity().markDirty()

            // TODO: investigate having node entities inside CC computers that hold power state instead

            val world = getWorld()
            val pos = getPos()
            val block = world.getBlockState(pos).block
            for (dir in Direction.values()) {
                if (CenterConnectionUtils.test(bundledOutputs, dir)) {
                    val offset = pos.offset(dir)
                    WorldUtils.updateNeighbor(world, offset, block, pos)
                }
            }
        }
    }

    override fun updatePower(inner: DyeColor, power: Int) {
        updatePower(BundledCableLogic.set(this.power, inner, power))
    }

    override fun getPower(inner: DyeColor): Int {
        return BundledCableLogic.get(power, inner)
    }

    override fun getPower(side: Direction): ULong {
        return if (RedstoneLogic.wiresGivePower
            && CenterConnectionUtils.test(connections, side)
            && !CenterConnectionUtils.test(blockage, side)
        ) power else 0u
    }
}
