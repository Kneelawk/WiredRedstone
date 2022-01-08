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
import com.kneelawk.wiredredstone.wirenet.NetNodeContainer
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes

abstract class AbstractRedstoneWirePart : AbstractConnectablePart, BlockablePart {
    var powered: Boolean
        private set

    /**
     * Blockage cache. This is for helping in determining in which directions to emit a redstone signal.
     */
    var blockage: UByte
        private set

    abstract val wireWidth: Double
    abstract val wireHeight: Double

    constructor(
        definition: PartDefinition, holder: MultipartHolder, side: Direction, connections: UByte, powered: Boolean,
        blockage: UByte
    ) : super(definition, holder, side, connections) {
        this.powered = powered
        this.blockage = blockage
    }

    constructor(definition: PartDefinition, holder: MultipartHolder, tag: NbtCompound) : super(
        definition, holder, tag
    ) {
        powered = tag.maybeGetByte("powered") == 1.toByte()
        blockage = tag.maybeGetByte("blockage")?.toUByte() ?: BlockageUtils.UNBLOCKED
    }

    constructor(definition: PartDefinition, holder: MultipartHolder, buffer: NetByteBuf, ctx: IMsgReadCtx) : super(
        definition, holder, buffer, ctx
    ) {
        powered = buffer.readBoolean()
        // There is currently no real use for blockage on the client
        blockage = BlockageUtils.UNBLOCKED
    }

    abstract fun isReceivingPower(): Boolean

    override fun toTag(): NbtCompound {
        val tag = super.toTag()
        tag.putByte("powered", if (powered) 1.toByte() else 0.toByte())
        tag.putByte("blockage", blockage.toByte())
        return tag
    }

    override fun writeCreationData(buffer: NetByteBuf, ctx: IMsgWriteCtx) {
        super.writeCreationData(buffer, ctx)
        buffer.writeBoolean(powered)
    }

    override fun writeRenderData(buffer: NetByteBuf, ctx: IMsgWriteCtx) {
        super.writeRenderData(buffer, ctx)
        buffer.writeBoolean(powered)
    }

    override fun readRenderData(buffer: NetByteBuf, ctx: IMsgReadCtx) {
        super.readRenderData(buffer, ctx)
        powered = buffer.readBoolean()
    }

    override fun onAdded(bus: MultipartEventBus) {
        super.onAdded(bus)
        getProperties().setValue(this, MultipartProperties.CAN_EMIT_REDSTONE, true)

        bus.addListener(this, NeighbourUpdateEvent::class.java) {
            updateConnections()
        }

        bus.addListener(this, PartAddedEvent::class.java) { e ->
            // NetNodeContainers update our connections directly when changed
            if (e.part !is NetNodeContainer) {
                updateConnections()
            }
        }

        bus.addListener(this, PartRemovedEvent::class.java) { e ->
            // NetNodeContainers update our connections directly when changed
            if (e.removed !is NetNodeContainer) {
                updateConnections()
            }
        }
    }

    private fun updateConnections() {
        val world = getWorld()
        if (world is ServerWorld) {
            ConnectableUtils.updateBlockageAndConnections(world, getSidedPos(), wireWidth, wireHeight)
            RedstoneLogic.wiresGivePower = false
            if (isReceivingPower() != powered) {
                RedstoneLogic.scheduleUpdate(world, getPos())
            }
            RedstoneLogic.wiresGivePower = true
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
                    if (state.emitsRedstonePower() && otherPart.allParts.none { it is AbstractConnectablePart }) {
                        newConn = ConnectionUtils.setExternal(newConn, cardinal)
                    }
                } else {
                    if (state.emitsRedstonePower()) {
                        newConn = ConnectionUtils.setExternal(newConn, cardinal)
                    }
                }
            }
        }

        return newConn
    }

    override fun updateBlockage(blockage: UByte) {
        this.blockage = blockage
        getBlockEntity().markDirty()
    }

    fun updatePowered(powered: Boolean) {
        val changed = this.powered != powered
        this.powered = powered
        getBlockEntity().markDirty()

        if (changed) {
            // update neighbors
            val world = getWorld()
            val pos = getPos()
            val state = world.getBlockState(pos)
            val offset1 = pos.offset(side)

            // Not really sure if this is necessary
            world.updateListeners(pos, state, state, Block.NOTIFY_ALL)

            world.updateNeighbors(pos, state.block)

            Direction.values()
                .filter { it != side.opposite }
                .map { offset1.offset(it) }
                .forEach { world.updateNeighbor(it, state.block, pos) }
        }
    }
}