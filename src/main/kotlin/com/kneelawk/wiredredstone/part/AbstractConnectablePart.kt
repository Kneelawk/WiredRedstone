package com.kneelawk.wiredredstone.part

import alexiil.mc.lib.multipart.api.MultipartHolder
import alexiil.mc.lib.multipart.api.PartDefinition
import alexiil.mc.lib.net.*
import com.kneelawk.wiredredstone.WRConstants.str
import com.kneelawk.wiredredstone.util.*
import net.minecraft.block.Block
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockView

/**
 * A part that is some kind of wire. It can have connections that are visible on the client.
 *
 * Subtypes for this could be redstone wires, bundle cables, or ribbon cables.
 */
abstract class AbstractConnectablePart : AbstractSidedPart {

    companion object {
        private val NET_PARENT: ParentNetIdSingle<AbstractConnectablePart> =
            NET_ID.subType(AbstractConnectablePart::class.java, str("abstract_wire_part"))

        private val NET_REDRAW: NetIdSignalK<AbstractConnectablePart> =
            NET_PARENT.idSignal("redraw").toClientOnly().setRecv {
                redraw()
            }

        fun getWire(world: BlockView, pos: SidedPos): AbstractConnectablePart? {
            return getPart(world, pos) as? AbstractConnectablePart
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

    open fun overrideConnections(connections: UByte): UByte {
        return connections
    }

    fun updateConnections(connections: UByte) {
        this.connections = connections
        getBlockEntity().markDirty()

        // Not really sure if this is necessary
        val pos = getPos()
        val world = getWorld()
        val state = world.getBlockState(pos)
        world.updateListeners(pos, state, state, Block.NOTIFY_ALL)
    }
}
