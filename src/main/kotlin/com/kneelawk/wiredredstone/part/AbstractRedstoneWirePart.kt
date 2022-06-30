package com.kneelawk.wiredredstone.part

import alexiil.mc.lib.multipart.api.MultipartEventBus
import alexiil.mc.lib.multipart.api.MultipartHolder
import alexiil.mc.lib.multipart.api.MultipartUtil
import alexiil.mc.lib.multipart.api.PartDefinition
import alexiil.mc.lib.multipart.api.event.NeighbourUpdateEvent
import alexiil.mc.lib.multipart.api.event.PartAddedEvent
import alexiil.mc.lib.multipart.api.event.PartRemovedEvent
import alexiil.mc.lib.multipart.api.property.MultipartProperties
import alexiil.mc.lib.net.IMsgReadCtx
import alexiil.mc.lib.net.IMsgWriteCtx
import alexiil.mc.lib.net.NetByteBuf
import com.kneelawk.wiredredstone.util.*
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes

abstract class AbstractRedstoneWirePart : AbstractBlockablePart, PowerablePart {
    var power: Int
        private set

    private val redstoneCache = mutableMapOf<BlockPos, Boolean>()

    abstract val wireWidth: Double
    abstract val wireHeight: Double

    constructor(
        definition: PartDefinition, holder: MultipartHolder, side: Direction, connections: UByte, power: Int,
        blockage: UByte
    ) : super(definition, holder, side, connections, blockage) {
        this.power = power
    }

    constructor(definition: PartDefinition, holder: MultipartHolder, tag: NbtCompound) : super(
        definition, holder, tag
    ) {
        power = tag.maybeGetByte("power")?.toInt()?.coerceIn(0..15)
            ?: if (tag.maybeGetByte("powered") == 1.toByte()) 15 else 0
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
                    updateConnections(world)
                }

                maybeScheduleUpdate(world)
            }
        }

        bus.addListener(this, PartAddedEvent::class.java) { e ->
            // NetNodeContainers update our connections directly when changed
            val world = getWorld()
            if (world is ServerWorld && e.part !is BlockNodeContainer) {
                updateConnections(world)
                maybeScheduleUpdate(world)
            }
        }

        bus.addListener(this, PartRemovedEvent::class.java) { e ->
            // NetNodeContainers update our connections directly when changed
            val world = getWorld()
            if (world is ServerWorld && e.removed !is BlockNodeContainer) {
                updateConnections(world)
                maybeScheduleUpdate(world)
            }
        }
    }

    private fun maybeScheduleUpdate(world: ServerWorld) {
        RedstoneLogic.wiresGivePower = false
        if (getReceivingPower() != power) {
            RedstoneLogic.scheduleUpdate(world, getPos())
        }
        RedstoneLogic.wiresGivePower = true
    }

    fun updateConnections(world: ServerWorld) {
        ConnectableUtils.updateBlockageAndConnections(world, this, wireWidth, wireHeight)
    }

    override fun onRemoved() {
        super.onRemoved()

        if (!isClientSide()) {
            WorldUtils.strongUpdateAllNeighbors(getWorld(), getPos(), side)
        }
    }

    override fun getCollisionShape(): VoxelShape {
        return VoxelShapes.empty()
    }

    override fun getClosestBlockState(): BlockState {
        return Blocks.REDSTONE_BLOCK.defaultState
    }

    override fun calculateBreakingDelta(player: PlayerEntity): Float {
        // Break wires instantly like redstone wire
        return super.calculateBreakingDelta(player, Blocks.REDSTONE_WIRE)
    }

    override fun getCullingShape(): VoxelShape {
        return VoxelShapes.empty()
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
                val state = world.getBlockState(offset)
                val otherPart = MultipartUtil.get(world, offset)
                if (otherPart != null) {
                    // TODO: implement better multipart redstone connection
                    if (state.emitsRedstonePower() && otherPart.allParts.none { it is ConnectablePart }) {
                        newConn = ConnectionUtils.setExternal(newConn, cardinal)
                    }
                } else {
                    if (RedstoneLogic.shouldWireConnect(state)) {
                        newConn = ConnectionUtils.setExternal(newConn, cardinal)
                    }
                }
            }
        }

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
            WorldUtils.strongUpdateAllNeighbors(world, pos, side)
        }
    }
}
