package com.kneelawk.wiredredstone.part

import alexiil.mc.lib.multipart.api.MultipartEventBus
import alexiil.mc.lib.multipart.api.MultipartHolder
import alexiil.mc.lib.multipart.api.PartDefinition
import alexiil.mc.lib.multipart.api.event.NeighbourUpdateEvent
import alexiil.mc.lib.multipart.api.event.PartAddedEvent
import alexiil.mc.lib.multipart.api.event.PartRemovedEvent
import alexiil.mc.lib.multipart.api.property.MultipartProperties
import alexiil.mc.lib.net.IMsgReadCtx
import alexiil.mc.lib.net.IMsgWriteCtx
import alexiil.mc.lib.net.NetByteBuf
import com.kneelawk.wiredredstone.logic.RedstoneLogic
import com.kneelawk.wiredredstone.util.*
import com.kneelawk.wiredredstone.util.connectable.ConnectableUtils
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos

abstract class AbstractCenterRedstoneWirePart : AbstractCenterBlockablePart, PowerablePart {
    var power: Int
        private set

    private val redstoneCache = mutableMapOf<BlockPos, Boolean>()

    abstract val wireDiameter: Double

    constructor(
        definition: PartDefinition, holder: MultipartHolder, connections: UByte, blockage: UByte, power: Int
    ) : super(
        definition, holder, connections, blockage
    ) {
        this.power = power
    }

    constructor(definition: PartDefinition, holder: MultipartHolder, tag: NbtCompound) : super(
        definition, holder, tag
    ) {
        power = tag.getByte("power").toInt().coerceIn(0..15)
    }

    constructor(definition: PartDefinition, holder: MultipartHolder, buffer: NetByteBuf, ctx: IMsgReadCtx) : super(
        definition, holder, buffer, ctx
    ) {
        power = buffer.readFixedBits(4).coerceIn(0..15)
    }

    abstract fun getReceivingPower(): Int

    override fun toTag(): NbtCompound {
        val tag = super.toTag()
        tag.putByte("power", power.toByte())
        return tag
    }

    override fun writeCreationData(buffer: NetByteBuf, ctx: IMsgWriteCtx) {
        super.writeCreationData(buffer, ctx)
        buffer.writeFixedBits(power, 4)
    }

    override fun writeRenderData(buffer: NetByteBuf, ctx: IMsgWriteCtx) {
        super.writeRenderData(buffer, ctx)
        buffer.writeFixedBits(power, 4)
    }

    override fun readRenderData(buffer: NetByteBuf, ctx: IMsgReadCtx) {
        super.readRenderData(buffer, ctx)
        power = buffer.readFixedBits(4).coerceIn(0..15)
    }

    override fun onAdded(bus: MultipartEventBus) {
        super.onAdded(bus)
        getProperties().setValue(this, MultipartProperties.CAN_EMIT_REDSTONE, true)

        bus.addListener(this, NeighbourUpdateEvent::class.java) {
            // Sometimes this gets called after this part has been removed already
            if (isRemoved()) return@addListener

            val world = getWorld()
            if (world is ServerWorld) {
                // updating connections, so we want to make sure we *really* need to do it first
                if (ConnectableUtils.shouldUpdateForNeighborUpdate(
                        redstoneCache, getPos(), it.pos,
                        { RedstoneLogic.shouldWireConnect(world.getBlockState(it.pos)) },
                        { prev, cur -> prev != cur })
                ) {
                    updateInternalConnections(world)
                }

                maybeScheduleUpdate(world)
            }
        }

        bus.addListener(this, PartAddedEvent::class.java) { e ->
            // NetNodeContainers update our connections directly when changed
            val world = getWorld()
            if (world is ServerWorld && e.part !is BlockNodeContainer) {
                updateInternalConnections(world)
                maybeScheduleUpdate(world)
            }
        }

        bus.addListener(this, PartRemovedEvent::class.java) { e ->
            // NetNodeContainers update our connections directly when changed
            val world = getWorld()
            if (world is ServerWorld && e.removed !is BlockNodeContainer) {
                updateInternalConnections(world)
                maybeScheduleUpdate(world)
            }
        }
    }

    private fun maybeScheduleUpdate(world: ServerWorld) {
        // If this true, we know this update was caused by another wire
        if (WorldUtils.doingUpdate) return

        RedstoneLogic.wiresGivePower = false
        if (getReceivingPower() != power) {
            RedstoneLogic.scheduleUpdate(world, getPos())
        }
        RedstoneLogic.wiresGivePower = true
    }

    fun updateInternalConnections(world: ServerWorld) {
        ConnectableUtils.updateBlockageAndConnections(world, this, wireDiameter)
    }

    override fun onRemoved() {
        super.onRemoved()

        if (!isClientSide()) {
            WorldUtils.strongUpdateAllNeighbors(getWorld(), getPos(), connections)
        }
    }

    override fun getClosestBlockState(): BlockState {
        return Blocks.REDSTONE_BLOCK.defaultState
    }

    override fun calculateBreakingDelta(player: PlayerEntity): Float {
        // Break wires instantly like redstone wire
        return super.calculateBreakingDelta(player, Blocks.REDSTONE_WIRE)
    }

    override fun overrideConnections(connections: UByte): UByte {
        val world = getWorld()
        val pos = getPos()
        var newConn = connections

        // TODO

        return newConn
    }

    override fun updatePower(power: Int) {
        val changed = this.power != power
        this.power = power
        getBlockEntity().markDirty()

        if (changed) {
            // update neighbors
            val world = getWorld()
            val pos = getPos()
            WorldUtils.strongUpdateAllNeighbors(world, pos, connections)
        }
    }
}
