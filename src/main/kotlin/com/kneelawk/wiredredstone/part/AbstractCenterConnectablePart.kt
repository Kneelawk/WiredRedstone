package com.kneelawk.wiredredstone.part

import alexiil.mc.lib.multipart.api.MultipartHolder
import alexiil.mc.lib.multipart.api.PartDefinition
import alexiil.mc.lib.net.*
import com.kneelawk.wiredredstone.WRConstants
import com.kneelawk.wiredredstone.util.getBlockEntity
import com.kneelawk.wiredredstone.util.isClientSide
import com.kneelawk.wiredredstone.util.isRemoved
import com.kneelawk.wiredredstone.util.setRecv
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes

abstract class AbstractCenterConnectablePart : AbstractWRPart, CenterConnectablePart, RedrawablePart {

    companion object {
        fun initNetworking() {}

        private val NET_PARENT: ParentNetIdSingle<AbstractCenterConnectablePart> =
            NET_ID.subType(
                AbstractCenterConnectablePart::class.java, WRConstants.str("abstract_center_connectable_part")
            )

        private val NET_RECALCULATE_SHAPE: NetIdSignalK<AbstractCenterConnectablePart> =
            NET_PARENT.idSignal("recalculate_shape").toClientOnly().setRecv {
                reshape()
            }
    }

    /**
     * Connections serves two functions. First, this is the data sent to the client so wires will actually render their
     * connections. Second, this data controls which sides wires can accept external redstone signals from.
     */
    var connections: UByte
        private set

    constructor(definition: PartDefinition, holder: MultipartHolder, connections: UByte) : super(definition, holder) {
        this.connections = connections
    }

    constructor(definition: PartDefinition, holder: MultipartHolder, tag: NbtCompound) : super(
        definition, holder, tag
    ) {
        connections = tag.getByte("connections").toUByte()
    }

    constructor(definition: PartDefinition, holder: MultipartHolder, buffer: NetByteBuf, ctx: IMsgReadCtx) : super(
        definition, holder
    ) {
        connections = buffer.readFixedBits(6).toUByte()
    }

    override fun toTag(): NbtCompound {
        val tag = super.toTag()
        tag.putByte("connections", connections.toByte())
        return tag
    }

    override fun writeCreationData(buffer: NetByteBuf, ctx: IMsgWriteCtx) {
        super.writeCreationData(buffer, ctx)
        buffer.writeFixedBits(connections.toInt(), 6)
    }

    override fun writeRenderData(buffer: NetByteBuf, ctx: IMsgWriteCtx) {
        super.writeRenderData(buffer, ctx)
        buffer.writeFixedBits(connections.toInt(), 6)
    }

    override fun readRenderData(buffer: NetByteBuf, ctx: IMsgReadCtx) {
        super.readRenderData(buffer, ctx)
        connections = buffer.readFixedBits(6).toUByte()
    }

    override fun redraw() {
        // Sometimes this gets called after a part has already been removed
        if (isRemoved()) return

        if (!isClientSide()) {
            sendNetworkUpdate(this, NET_RENDER_DATA)
        }

        // handles sending redraw packets to the client
        redrawIfChanged()
    }

    override fun reshape() {
        // Want to make sure nothing bad happens if this is called after this part has already been removed.
        if (isRemoved()) return

        if (!isClientSide()) {
            sendNetworkUpdate(this, NET_RECALCULATE_SHAPE)
        }

        holder.container.recalculateShape()
    }

    override fun updateConnections(connections: UByte) {
        this.connections = connections
        getBlockEntity().markDirty()
    }

    override fun overrideConnections(connections: UByte): UByte = connections
}
