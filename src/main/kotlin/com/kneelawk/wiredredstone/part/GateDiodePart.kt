package com.kneelawk.wiredredstone.part

import alexiil.mc.lib.multipart.api.MultipartEventBus
import alexiil.mc.lib.multipart.api.MultipartHolder
import alexiil.mc.lib.multipart.api.PartDefinition
import alexiil.mc.lib.multipart.api.event.NeighbourUpdateEvent
import alexiil.mc.lib.multipart.api.event.PartRedstonePowerEvent
import alexiil.mc.lib.multipart.api.event.PartTickEvent
import alexiil.mc.lib.multipart.api.property.MultipartProperties
import alexiil.mc.lib.multipart.api.render.PartModelKey
import alexiil.mc.lib.net.IMsgReadCtx
import alexiil.mc.lib.net.IMsgWriteCtx
import alexiil.mc.lib.net.NetByteBuf
import com.kneelawk.wiredredstone.item.WRItems
import com.kneelawk.wiredredstone.part.key.GateDiodePartKey
import com.kneelawk.wiredredstone.partext.GateDiodePartExt
import com.kneelawk.wiredredstone.util.*
import com.kneelawk.wiredredstone.wirenet.getWireNetworkState
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.loot.context.LootContext
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape

class GateDiodePart : AbstractRotatedPart {
    companion object {
        const val CONNECTION_WIDTH = 2.0
        const val CONNECTION_HEIGHT = 2.0

        val CONNECTION_BLOCKING_SHAPES =
            BoundingBoxUtils.getRotatedShapes(Box(4.0 / 16.0, 0.0, 4.0 / 16.0, 12.0 / 16.0, 2.0 / 16.0, 12.0 / 16.0))
        val SHAPES = BoundingBoxUtils.getRotatedShapes(Box(0.0, 0.0, 0.0, 1.0, 2.0 / 16.0, 1.0))
    }

    override val partExtType = GateDiodePartExt.Type

    var inputPower: Int
        private set
    var outputPower: Int
        private set
    // Gates with more inputs might find it most efficient to just have a `shouldUpdate` variable, but since there is
    // only one input and one output and a pure function between them, we can always tell if we need an update just by
    // looking at the two of these.

    constructor(
        definition: PartDefinition, holder: MultipartHolder, side: Direction, connections: UByte, direction: Direction,
        inputPower: Int, outputPower: Int
    ) : super(definition, holder, side, connections, direction) {
        this.inputPower = inputPower
        this.outputPower = outputPower
    }

    constructor(definition: PartDefinition, holder: MultipartHolder, tag: NbtCompound) : super(
        definition, holder, tag
    ) {
        inputPower = tag.getByte("inputPower").toInt().coerceIn(0..15)
        outputPower = tag.getByte("outputPower").toInt().coerceIn(0..15)
    }

    constructor(definition: PartDefinition, holder: MultipartHolder, buffer: NetByteBuf, ctx: IMsgReadCtx) : super(
        definition, holder, buffer, ctx
    ) {
        inputPower = buffer.readByte().toInt().coerceIn(0..15)
        outputPower = buffer.readByte().toInt().coerceIn(0..15)
    }

    override fun toTag(): NbtCompound {
        val tag = super.toTag()
        tag.putByte("inputPower", inputPower.toByte())
        tag.putByte("outputPower", outputPower.toByte())
        return tag
    }

    override fun writeCreationData(buffer: NetByteBuf, ctx: IMsgWriteCtx) {
        super.writeCreationData(buffer, ctx)
        buffer.writeByte(inputPower)
        buffer.writeByte(outputPower)
    }

    override fun writeRenderData(buffer: NetByteBuf, ctx: IMsgWriteCtx) {
        super.writeRenderData(buffer, ctx)
        buffer.writeByte(inputPower)
        buffer.writeByte(outputPower)
    }

    override fun readRenderData(buffer: NetByteBuf, ctx: IMsgReadCtx) {
        super.readRenderData(buffer, ctx)
        inputPower = buffer.readByte().toInt().coerceIn(0..15)
        outputPower = buffer.readByte().toInt().coerceIn(0..15)
    }

    override fun onAdded(bus: MultipartEventBus) {
        super.onAdded(bus)
        getProperties().setValue(this, MultipartProperties.CAN_EMIT_REDSTONE, true)

        bus.addListener(this, PartTickEvent::class.java) {
            val world = getWorld()
            if (world is ServerWorld) {
                // We do this here so that the delay between input and output is always the same
                // regardless of wirenet update order.
                if (outputPower != inputPower) {
                    outputPower = inputPower

                    val pos = getPos()
                    RedstoneLogic.scheduleUpdate(world, pos)
                    redraw()

                    // Update neighbors
                    val edge = RotationUtils.rotatedDirection(side, direction)
                    WorldUtils.strongUpdateNeighbors(world, pos, edge)
                }
            }
        }

        bus.addListener(this, NeighbourUpdateEvent::class.java) {
            // Sometimes this gets called after this part has been removed already
            if (isRemoved()) return@addListener

            val world = getWorld()
            if (world is ServerWorld) {
                // Something could be blocking our connection
                world.getWireNetworkState().controller.updateConnections(world, getSidedPos())

                updateConnections()
                RedstoneLogic.wiresGivePower = false
                if (calculateInputPower() != inputPower) {
                    RedstoneLogic.scheduleUpdate(world, getPos())
                }
                RedstoneLogic.wiresGivePower = true
            }
        }

        bus.addListener(this, PartRedstonePowerEvent::class.java) { e ->
            e.set(getRedstoneOutputPower(e.side))
        }
    }

    override fun onRemoved() {
        super.onRemoved()

        if (!isClientSide()) {
            val edge = RotationUtils.rotatedDirection(side, direction)
            WorldUtils.strongUpdateNeighbors(getWorld(), getPos(), edge)
        }
    }

    private fun getRedstoneOutputPower(powerSide: Direction): Int {
        val edge = RotationUtils.rotatedDirection(side, direction)
        return if (RedstoneLogic.wiresGivePower && powerSide == edge) outputPower else 0
    }

    fun calculateInputPower(): Int {
        val edge = RotationUtils.rotatedDirection(side, getInputSide())
        return getWorld().getEmittedRedstonePower(getPos().offset(edge), edge)
    }

    /**
     * Gets the cardinal direction of the input side.
     */
    fun getInputSide(): Direction {
        return direction.opposite
    }

    /**
     * Gets the cardinal direction of the output side.
     */
    fun getOutputSide(): Direction {
        return direction
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

    override fun getModelKey(): PartModelKey {
        return GateDiodePartKey(side, direction, outputPower != 0)
    }

    override fun getClosestBlockState(): BlockState {
        return Blocks.REPEATER.defaultState
    }

    override fun getPickStack(hit: BlockHitResult?): ItemStack {
        return ItemStack(WRItems.GATE_DIODE)
    }

    override fun addDrops(target: ItemDropTarget, context: LootContext) {
        LootTableUtil.addPartDrops(getWorld(), target, context, WRParts.GATE_DIODE.identifier)
    }

    fun updateInputPower(power: Int) {
        this.inputPower = power
    }
}