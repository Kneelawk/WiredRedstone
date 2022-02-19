package com.kneelawk.wiredredstone.part

import alexiil.mc.lib.multipart.api.MultipartHolder
import alexiil.mc.lib.multipart.api.PartDefinition
import alexiil.mc.lib.net.*
import com.kneelawk.wiredredstone.WRConstants.str
import com.kneelawk.wiredredstone.util.*
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape

/**
 * A part that is some kind of wire. It can have connections that are visible on the client.
 *
 * Subtypes for this could be redstone wires, bundle cables, or ribbon cables.
 */
abstract class AbstractConnectablePart : AbstractSidedPart, ConnectablePart, RedrawablePart {

    companion object {
        private val NET_PARENT: ParentNetIdSingle<AbstractConnectablePart> =
            NET_ID.subType(AbstractConnectablePart::class.java, str("abstract_wire_part"))

        private val NET_REDRAW: NetIdSignalK<AbstractConnectablePart> =
            NET_PARENT.idSignal("redraw").toClientOnly().setRecv {
                redraw()
            }
    }

    /**
     * Connections serves two functions. First, this is the data sent to the client so wires will actually render their
     * connections. Second, this data controls which sides wires can accept external redstone signals from.
     */
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

    override fun redraw() {
        // Sometimes this gets called after a part has already been removed
        if (isRemoved()) return

        if (isClientSide()) {
            redrawIfChanged()
        } else {
            sendNetworkUpdate(this, NET_RENDER_DATA)
            sendNetworkUpdate(this, NET_REDRAW)
        }
        holder.container.recalculateShape()
    }

    override fun overrideConnections(connections: UByte): UByte {
        return connections
    }

    override fun updateConnections(connections: UByte) {
        this.connections = connections
        getBlockEntity().markDirty()
    }

    override fun getConnectionBlockingShape(): VoxelShape {
        return shape
    }
}
