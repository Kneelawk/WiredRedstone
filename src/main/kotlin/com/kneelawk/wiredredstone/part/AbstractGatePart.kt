package com.kneelawk.wiredredstone.part

import alexiil.mc.lib.multipart.api.MultipartEventBus
import alexiil.mc.lib.multipart.api.MultipartHolder
import alexiil.mc.lib.multipart.api.PartDefinition
import alexiil.mc.lib.multipart.api.event.NeighbourUpdateEvent
import alexiil.mc.lib.multipart.api.property.MultipartProperties
import alexiil.mc.lib.net.IMsgReadCtx
import alexiil.mc.lib.net.NetByteBuf
import com.kneelawk.wiredredstone.logic.RedstoneLogic
import com.kneelawk.wiredredstone.util.BoundingBoxUtils
import com.kneelawk.wiredredstone.util.connectable.ConnectableUtils
import com.kneelawk.wiredredstone.util.getProperties
import com.kneelawk.wiredredstone.util.getWorld
import com.kneelawk.wiredredstone.util.isRemoved
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape

abstract class AbstractGatePart : AbstractRotatedPart {
    companion object {
        const val CONNECTION_WIDTH = 2.0
        const val CONNECTION_HEIGHT = 2.0

        val CONNECTION_BLOCKING_SHAPES =
            BoundingBoxUtils.getRotatedShapes(Box(4.0 / 16.0, 0.0, 4.0 / 16.0, 12.0 / 16.0, 2.0 / 16.0, 12.0 / 16.0))
        val SHAPES = BoundingBoxUtils.getRotatedShapes(Box(0.0, 0.0, 0.0, 1.0, 2.0 / 16.0, 1.0))
    }

    constructor(
        definition: PartDefinition, holder: MultipartHolder, side: Direction, connections: UByte, direction: Direction
    ) : super(definition, holder, side, connections, direction)

    constructor(definition: PartDefinition, holder: MultipartHolder, tag: NbtCompound) : super(definition, holder, tag)
    constructor(definition: PartDefinition, holder: MultipartHolder, buffer: NetByteBuf, ctx: IMsgReadCtx) : super(
        definition, holder, buffer, ctx
    )

    abstract fun shouldScheduleUpdate(): Boolean

    override fun onAdded(bus: MultipartEventBus) {
        super.onAdded(bus)
        getProperties().setValue(this, MultipartProperties.CAN_EMIT_REDSTONE, true)

        bus.addListener(this, NeighbourUpdateEvent::class.java) {
            // Sometimes this gets called after this part has been removed already
            if (isRemoved()) return@addListener

            val world = getWorld()
            if (world is ServerWorld) {
                updateConnections()
                RedstoneLogic.wiresGivePower = false
                if (shouldScheduleUpdate()) {
                    RedstoneLogic.scheduleUpdate(world, getPos())
                }
                RedstoneLogic.wiresGivePower = true
            }
        }
    }

    fun updateConnections() {
        val world = getWorld()
        if (world is ServerWorld) {
            ConnectableUtils.updateConnections(world, this)
        }
    }

    override fun getShape(): VoxelShape {
        return SHAPES[side]!!
    }

    override fun getConnectionBlockingShape(): VoxelShape {
        return CONNECTION_BLOCKING_SHAPES[side]!!
    }

    override fun getClosestBlockState(): BlockState {
        return Blocks.REPEATER.defaultState
    }
}
