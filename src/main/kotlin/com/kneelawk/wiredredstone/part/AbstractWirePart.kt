package com.kneelawk.wiredredstone.part

import alexiil.mc.lib.multipart.api.MultipartHolder
import alexiil.mc.lib.multipart.api.PartDefinition
import alexiil.mc.lib.net.*
import com.kneelawk.wiredredstone.WRConstants.str
import com.kneelawk.wiredredstone.util.*
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView

/**
 * A part that is some kind of wire. It can have connections that are visible on the client.
 *
 * Subtypes for this could be redstone wires, bundle cables, or ribbon cables.
 */
abstract class AbstractWirePart : AbstractSidedPart {

    companion object {
        private val NET_PARENT: ParentNetIdSingle<AbstractWirePart> =
            NET_ID.subType(AbstractWirePart::class.java, str("abstract_wire_part"))

        private val NET_REDRAW: NetIdSignalK<AbstractWirePart> = NET_PARENT.idSignal("redraw").setRecv {
            redraw()
        }

        fun getWire(world: BlockView, pos: SidedPos): AbstractWirePart? {
            return getPart(world, pos) as? AbstractWirePart
        }
    }

    var connections: UByte
        private set

    constructor(definition: PartDefinition, holder: MultipartHolder, side: Direction, connections: UByte) : super(
        definition, holder, side
    ) {
        this.connections = connections
    }

    constructor(definition: PartDefinition, holder: MultipartHolder, tag: NbtCompound) : super(
        definition, holder, tag
    ) {
        connections = tag.maybeGetByte("connections")?.toUByte() ?: 0u
    }

    constructor(definition: PartDefinition, holder: MultipartHolder, buffer: NetByteBuf, ctx: IMsgReadCtx) : super(
        definition, holder, buffer, ctx
    ) {
        connections = buffer.readByte().toUByte()
    }

    // TODO: Detect dependant blocks breaking or becoming non-solid

    override fun toTag(): NbtCompound {
        val tag = super.toTag()
        tag.putByte("connections", connections.toByte())
        return tag
    }

    override fun writeCreationData(buffer: NetByteBuf, ctx: IMsgWriteCtx) {
        super.writeCreationData(buffer, ctx)
        buffer.writeByte(connections.toInt())
    }

    override fun writeRenderData(buffer: NetByteBuf, ctx: IMsgWriteCtx) {
        super.writeRenderData(buffer, ctx)
        buffer.writeByte(connections.toInt())
    }

    override fun readRenderData(buffer: NetByteBuf, ctx: IMsgReadCtx) {
        super.readRenderData(buffer, ctx)
        connections = buffer.readByte().toUByte()
    }

    fun redraw() {
        if (isClientSide()) {
            redrawIfChanged()
        } else {
            sendNetworkUpdate(this, NET_RENDER_DATA)
            sendNetworkUpdate(this, NET_REDRAW)
        }
        holder.container.recalculateShape()
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

    open fun overrideConnections(connections: UByte): UByte {
        return connections
    }

    fun updateConnections(connections: UByte) {
        this.connections = connections
        getBlockEntity().markDirty()

        val pos = getPos()
        val world = getWorld()
        val state = world.getBlockState(pos)
        world.updateListeners(pos, state, state, 3)
    }
}
