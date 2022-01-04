package com.kneelawk.wiredredstone.part

import alexiil.mc.lib.multipart.api.MultipartEventBus
import alexiil.mc.lib.multipart.api.MultipartHolder
import alexiil.mc.lib.multipart.api.MultipartUtil
import alexiil.mc.lib.multipart.api.PartDefinition
import alexiil.mc.lib.multipart.api.event.NeighbourUpdateEvent
import alexiil.mc.lib.multipart.api.property.MultipartProperties
import alexiil.mc.lib.net.IMsgReadCtx
import alexiil.mc.lib.net.IMsgWriteCtx
import alexiil.mc.lib.net.NetByteBuf
import com.kneelawk.wiredredstone.util.*
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.Direction

abstract class AbstractRedstoneWirePart : AbstractWirePart {
    var powered: Boolean
        private set

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
            val world = getWorld()
            if (world is ServerWorld) {
                WireUtils.updateClientWire(world, getSidedPos())
                RedstoneLogic.wiresGivePower = false
                if (isReceivingPower() != powered) {
                    RedstoneLogic.scheduleUpdate(world, getPos())
                }
                RedstoneLogic.wiresGivePower = true
            }
        }
    }

    override fun overrideConnections(connections: UByte): UByte {
        val world = getWorld()
        var newConn = connections

        for (cardinal in DirectionUtils.HORIZONTALS) {
            if (ConnectionUtils.isDisconnected(connections, cardinal)) {
                val edge = RotationUtils.rotatedDirection(side, cardinal)
                val offset = getPos().offset(edge)
                val otherPart = MultipartUtil.get(world, offset)
                if (otherPart != null) {
                    // TODO: implement multipart redstone connection
                } else {
                    val state = world.getBlockState(offset)
                    if (state.emitsRedstonePower()) {
                        newConn = ConnectionUtils.setExternal(newConn, cardinal)
                    }
                }
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
            world.updateNeighbors(pos, state.block)

            Direction.values()
                .filter { it != side.opposite }
                .map { offset1.offset(it) }
                .forEach { world.updateNeighbor(it, state.block, pos) }
        }
    }
}