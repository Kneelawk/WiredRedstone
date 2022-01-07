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
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes

abstract class AbstractRedstoneWirePart : AbstractConnectablePart {
    var powered: Boolean
        private set

    abstract val wireWidth: Double
    abstract val wireHeight: Double

    constructor(
        definition: PartDefinition, holder: MultipartHolder, side: Direction, connections: UByte, powered: Boolean
    ) : super(definition, holder, side, connections) {
        this.powered = powered
    }

    constructor(definition: PartDefinition, holder: MultipartHolder, tag: NbtCompound) : super(
        definition, holder, tag
    ) {
        powered = tag.maybeGetByte("powered") == 1.toByte()
    }

    constructor(definition: PartDefinition, holder: MultipartHolder, buffer: NetByteBuf, ctx: IMsgReadCtx) : super(
        definition, holder, buffer, ctx
    ) {
        powered = buffer.readBoolean()
    }

    abstract fun isReceivingPower(): Boolean

    override fun toTag(): NbtCompound {
        val tag = super.toTag()
        tag.putByte("powered", if (powered) 1.toByte() else 0.toByte())
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
            // TODO: Must change this if networkable part functionality is moved
            if (e.part !is AbstractSidedPart) {
                updateConnections()
            }
        }

        bus.addListener(this, PartRemovedEvent::class.java) { e ->
            // TODO: Must change this if networkable part functionality is moved
            if (e.removed !is AbstractSidedPart) {
                updateConnections()
            }
        }
    }

    private fun updateConnections() {
        val world = getWorld()
        if (world is ServerWorld) {
            ConnectableUtils.updateClientWire(world, getSidedPos())
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
        println("> overrideConnections($connections)")
        val world = getWorld()
        val pos = getPos()
        var newConn = connections

        for (cardinal in DirectionUtils.HORIZONTALS) {
            val edge = RotationUtils.rotatedDirection(side, cardinal)

            if (ConnectionUtils.isDisconnected(newConn, cardinal)) {
                println("Wire is currently disconnected")
                val offset = pos.offset(edge)
                val otherPart = MultipartUtil.get(world, offset)
                if (otherPart != null) {
                    // TODO: implement multipart redstone connection
                } else {
                    val state = world.getBlockState(offset)
                    println("state: $state")
                    if (state.emitsRedstonePower()) {
                        println("emitsRedstonePower(): true, connecting external")
                        newConn = ConnectionUtils.setExternal(newConn, cardinal)
                    }
                }
            }

            println("checking cardinal: $cardinal")
            val inside =
                BoundingBoxUtils.getWireInsideConnectionShape(side, edge, wireWidth, wireHeight) ?: continue
            if (ConnectableUtils.checkInside(world, pos, inside)) {
                println("checkInside(): true, disconnecting")
                newConn = ConnectionUtils.setDisconnected(newConn, cardinal)
            }
        }

        return newConn
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