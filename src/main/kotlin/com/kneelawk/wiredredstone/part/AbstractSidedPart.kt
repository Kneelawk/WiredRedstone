package com.kneelawk.wiredredstone.part

import alexiil.mc.lib.multipart.api.*
import alexiil.mc.lib.multipart.api.event.*
import alexiil.mc.lib.net.IMsgReadCtx
import alexiil.mc.lib.net.IMsgWriteCtx
import alexiil.mc.lib.net.NetByteBuf
import com.kneelawk.wiredredstone.node.WRBlockNodes.WIRE_NET
import com.kneelawk.wiredredstone.part.event.WRPartPreMoveEvent
import com.kneelawk.wiredredstone.util.*
import com.kneelawk.wiredredstone.util.connectable.ConnectableUtils
import net.minecraft.item.ItemStack
import net.minecraft.loot.context.LootContext
import net.minecraft.loot.context.LootContextParameterSet
import net.minecraft.loot.context.LootContextParameters
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import net.minecraft.util.shape.VoxelShape

/**
 * A part that is on the side of a block and can be part of the Redstone-ish network.
 *
 * Subtypes of this could be parts for wires, bundle cables, or gates.
 */
abstract class AbstractSidedPart :
    AbstractWRPart, BlockNodeContainer, SidedPart {

    final override var side: Direction
        private set

    constructor(definition: PartDefinition, holder: MultipartHolder, side: Direction) : super(definition, holder) {
        this.side = side
    }

    constructor(definition: PartDefinition, holder: MultipartHolder, tag: NbtCompound) : super(definition, holder, tag) {
        this.side = Direction.byId(tag.getByte("side").toInt())
    }

    constructor(definition: PartDefinition, holder: MultipartHolder, buffer: NetByteBuf, ctx: IMsgReadCtx) : super(definition, holder) {
        this.side = Direction.byId(buffer.readFixedBits(3))
    }

    override fun toTag(): NbtCompound {
        val tag = super.toTag()
        tag.putByte("side", side.id.toByte())

        return tag
    }

    override fun writeCreationData(buffer: NetByteBuf, ctx: IMsgWriteCtx) {
        super.writeCreationData(buffer, ctx)
        buffer.writeFixedBits(side.id, 3)
    }

    override fun writeRenderData(buffer: NetByteBuf, ctx: IMsgWriteCtx) {
        super.writeRenderData(buffer, ctx)
        buffer.writeFixedBits(side.id, 3)
    }

    override fun readRenderData(buffer: NetByteBuf, ctx: IMsgReadCtx) {
        super.readRenderData(buffer, ctx)
        // No SideContext on the client-side
        side = Direction.byId(buffer.readFixedBits(3))
    }

    override fun onAdded(bus: MultipartEventBus) {
        super.onAdded(bus)

        // nothing else here needs to be done on the client
        if (getWorld().isClient) return

        // Rotation Handling

        bus.addListener(this, PartTransformEvent::class.java) { e ->
            side = e.transformation.map(side)
        }

        bus.addContextlessListener(this, PartPostTransformEvent::class.java) {
            // Update the nodes here
            val world = getWorld()
            if (world is ServerWorld) {
                WIRE_NET.getServerGraphWorld(world).updateNodes(getPos())
            }
        }
    }

    override fun shouldBreak(): Boolean {
        val world = getWorld()
        val offset = getPos().offset(side)
        val state = world.getBlockState(offset)
        return !PlacementUtils.isValidFace(state, world, offset, side.opposite)
    }

    override fun updateConnections(world: ServerWorld) {
        WIRE_NET.getServerGraphWorld(world).updateConnections(getSidedPos())
    }
}
