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
import com.kneelawk.graphlib.api.node.KeyBlockNode
import com.kneelawk.graphlib.api.util.SidedPos
import com.kneelawk.wiredredstone.logic.BundledCableLogic
import com.kneelawk.wiredredstone.logic.RedstoneLogic
import com.kneelawk.wiredredstone.node.BundledCableBlockNode
import com.kneelawk.wiredredstone.part.key.BundledCablePartKey
import com.kneelawk.wiredredstone.util.*
import com.kneelawk.wiredredstone.util.bits.BlockageUtils
import com.kneelawk.wiredredstone.util.bits.ConnectionUtils
import com.kneelawk.wiredredstone.util.connectable.ConnectableUtils
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.loot.context.LootContextParameterSet
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

        // Defaults to ending block update in all directions, like previous version of WR. Though, technically, this
        // doesn't really matter, because wires recalculate their connections on chunk-load anyways.
        private val DEFAULT_BUNDLED_OUTPUTS: UByte

        init {
            var defaultBundledOutputs: UByte = 0u
            for (cardinal in DirectionUtils.HORIZONTALS) {
                defaultBundledOutputs = ConnectionUtils.setExternal(defaultBundledOutputs, cardinal)
            }
            DEFAULT_BUNDLED_OUTPUTS = defaultBundledOutputs
        }
    }

    val color: DyeColor?

    // Used for telling if any of our neighbors should receive block updates when we change state
    private var bundledOutputs: UByte
    private var bundledOutputDown: Boolean

    override var power: ULong
        private set

    constructor(
        definition: PartDefinition, holder: MultipartHolder, side: Direction, connections: UByte, blockage: UByte,
        color: DyeColor?, power: ULong, bundledOutputs: UByte, bundledOutputDown: Boolean
    ) : super(
        definition, holder, side, connections, blockage
    ) {
        this.color = color
        this.power = power
        this.bundledOutputs = bundledOutputs
        this.bundledOutputDown = bundledOutputDown
    }

    constructor(definition: PartDefinition, holder: MultipartHolder, tag: NbtCompound) : super(
        definition, holder, tag
    ) {
        color = if (tag.contains("color")) DyeColor.byId(tag.getByte("color").toInt()) else null
        power = if (tag.contains("power")) tag.getLong("power").toULong() else 0u
        bundledOutputs =
            if (tag.contains("bundledOutputs")) tag.getByte("bundledOutputs").toUByte() else DEFAULT_BUNDLED_OUTPUTS
        bundledOutputDown = if (tag.contains("bundledOutputDown")) tag.getBoolean("bundledOutputDown") else true
    }

    constructor(definition: PartDefinition, holder: MultipartHolder, buffer: NetByteBuf, ctx: IMsgReadCtx) : super(
        definition, holder, buffer, ctx
    ) {
        color = if (buffer.readBoolean()) DyeColor.byId(buffer.readByte().toInt()) else null
        // no need for power levels on the client for now
        power = 0u
        bundledOutputs = 0u
        bundledOutputDown = false
    }

    override fun createBlockNodes(): Collection<KeyBlockNode> {
        return DyeColor.values().asSequence().map { BundledCableBlockNode(side, color, it) }.toList()
    }

    override fun toTag(): NbtCompound {
        val tag = super.toTag()
        color?.let { tag.putByte("color", it.id.toByte()) }
        tag.putLong("power", power.toLong())
        tag.putByte("bundledOutputs", bundledOutputs.toByte())
        tag.putBoolean("bundledOutputDown", bundledOutputDown)
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
        // If this is true, we know this update was caused by another wire
        if (WorldUtils.doingUpdate) return

        val world = getWorld()
        if (world is ServerWorld) {
            ConnectableUtils.updateBlockageAndConnections(world, this, WIRE_WIDTH, WIRE_HEIGHT)
            if (BundledCableLogic.getBundledCableInput(world, getSidedPos(), connections, blockage) != power) {
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

    override fun addDrops(target: ItemDropTarget, params: LootContextParameterSet) {
        val base = WRParts.BUNDLED_CABLE.identifier
        val identifier = color?.let { Identifier(base.namespace, "${it.getName()}_${base.path}") } ?: base
        LootTableUtil.addPartDrops(getWorld(), target, params, identifier)
    }

    override fun overrideConnections(connections: UByte): UByte {
        val world = getWorld()
        val pos = getPos()
        var newConn = connections
        var newBundledOutputs: UByte = 0u

        for (cardinal in DirectionUtils.HORIZONTALS) {
            // Blockage gets updated before this gets called, so checking blockage here is ok
            if (!BlockageUtils.isBlocked(blockage, cardinal)) {
                val edge = RotationUtils.rotatedDirection(side, cardinal)
                val offset = pos.offset(edge)
                if (BundledCableLogic.hasBundledCableOutput(world, SidedPos(offset, edge.opposite))) {
                    newBundledOutputs = ConnectionUtils.setExternal(newBundledOutputs, cardinal)
                    if (ConnectionUtils.isDisconnected(newConn, cardinal)) {
                        newConn = ConnectionUtils.setExternal(newConn, cardinal)
                    }
                }
            }
        }

        bundledOutputs = newBundledOutputs

        // Also check downward for bundled outputs
        bundledOutputDown = BundledCableLogic.hasBundledCableOutput(world, SidedPos(pos.offset(side), side.opposite))

        return newConn
    }

    override fun updatePower(power: ULong) {
        val changed = this.power != power
        this.power = power

        getBlockEntity().markDirty()

        if (changed) {
            val world = getWorld()
            val pos = getPos()
            val block = world.getBlockState(pos).block
            for (cardinal in DirectionUtils.HORIZONTALS) {
                if (ConnectionUtils.isExternal(bundledOutputs, cardinal)) {
                    val edge = RotationUtils.rotatedDirection(side, cardinal)
                    val offset = pos.offset(edge)
                    WorldUtils.updateNeighbor(world, offset, block, pos)
                }
            }

            if (bundledOutputDown) {
                WorldUtils.updateNeighbor(world, pos.offset(side), block, pos)
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
        val cardinal = RotationUtils.unrotatedDirection(this.side, side)
        return if (RedstoneLogic.wiresGivePower
            && (side == this.side
                    || (DirectionUtils.isHorizontal(cardinal)
                    && !ConnectionUtils.isDisconnected(connections, cardinal)))
        ) power else 0uL
    }
}
