package com.kneelawk.wiredredstone.part

import alexiil.mc.lib.multipart.api.MultipartHolder
import alexiil.mc.lib.multipart.api.PartDefinition
import alexiil.mc.lib.multipart.api.render.PartModelKey
import alexiil.mc.lib.net.IMsgReadCtx
import alexiil.mc.lib.net.IMsgWriteCtx
import alexiil.mc.lib.net.NetByteBuf
import com.kneelawk.graphlib.api.graph.user.BlockNode
import com.kneelawk.wiredredstone.item.ProjectionViewerUtil
import com.kneelawk.wiredredstone.item.WRItems
import com.kneelawk.wiredredstone.logic.phantom.PhantomRedstone
import com.kneelawk.wiredredstone.logic.phantom.SidedPartPhantomRedstoneRef
import com.kneelawk.wiredredstone.node.GateProjectorSimpleBlockNode
import com.kneelawk.wiredredstone.part.key.GateProjectorSimplePartKey
import com.kneelawk.wiredredstone.util.*
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.loot.context.LootContextParameterSet
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction

class GateProjectorSimplePart : AbstractGatePart, SidedPhantomRedstoneProviderPart {
    companion object {
        private const val MAX_DISTANCE = 16
    }

    var inputPower: Int
        private set

    var storedDistance: Int
        private set

    val blockDistance: Int
        get() = storedDistance + 1

    constructor(
        definition: PartDefinition, holder: MultipartHolder, side: Direction, connections: UByte, direction: Direction,
        inputPower: Int, distance: Int
    ) : super(definition, holder, side, connections, direction) {
        this.inputPower = inputPower
        this.storedDistance = distance
    }

    constructor(definition: PartDefinition, holder: MultipartHolder, tag: NbtCompound) : super(
        definition, holder, tag
    ) {
        inputPower = tag.getByte("inputPower").toInt().coerceIn(0..15)
        storedDistance = tag.getByte("distance").toInt().coerceIn(0..15)
    }

    constructor(definition: PartDefinition, holder: MultipartHolder, buffer: NetByteBuf, ctx: IMsgReadCtx) : super(
        definition, holder, buffer, ctx
    ) {
        inputPower = buffer.readFixedBits(4)
        storedDistance = buffer.readFixedBits(4)
    }

    override fun toTag(): NbtCompound {
        val tag = super.toTag()
        tag.putByte("inputPower", inputPower.toByte())
        tag.putByte("distance", storedDistance.toByte())
        return tag
    }

    override fun writeCreationData(buffer: NetByteBuf, ctx: IMsgWriteCtx) {
        super.writeCreationData(buffer, ctx)
        buffer.writeFixedBits(inputPower, 4)
        buffer.writeFixedBits(storedDistance, 4)
    }

    override fun writeRenderData(buffer: NetByteBuf, ctx: IMsgWriteCtx) {
        super.writeRenderData(buffer, ctx)
        buffer.writeFixedBits(inputPower, 4)
        buffer.writeFixedBits(storedDistance, 4)
    }

    override fun readRenderData(buffer: NetByteBuf, ctx: IMsgReadCtx) {
        super.readRenderData(buffer, ctx)
        inputPower = buffer.readFixedBits(4)
        storedDistance = buffer.readFixedBits(4)
    }

    override fun createBlockNodes(): Collection<BlockNode> {
        return listOf(GateProjectorSimpleBlockNode(side))
    }

    override fun shouldScheduleUpdate(): Boolean {
        return calculateInputPower() != inputPower
    }

    override fun getModelKey(): PartModelKey {
        return GateProjectorSimplePartKey(side, direction, connections, inputPower != 0, blockDistance)
    }

    override fun getStrongRedstonePower(
        original: Int, world: ServerWorld, pos: BlockPos, oppositeFace: Direction
    ): Int {
        return 0
    }

    override fun getWeakRedstonePower(original: Int, world: ServerWorld, pos: BlockPos, oppositeFace: Direction): Int {
        return if (pos == getTarget() && oppositeFace.opposite == getOutputEdge()) {
            if (inputPower != 0) 15 else 0
        } else 0
    }

    override fun getPickStack(hitResult: BlockHitResult?): ItemStack {
        return ItemStack(WRItems.GATE_PROJECTOR_SIMPLE)
    }

    override fun addDrops(target: ItemDropTarget, params: LootContextParameterSet) {
        LootTableUtil.addPartDrops(this, target, params, WRParts.GATE_PROJECTOR_SIMPLE.identifier)
    }

    override fun onFirstTick() {
        super.onFirstTick()

        val world = getWorld()
        if (world is ServerWorld) {
            PhantomRedstone.addRef(world, getTarget(), SidedPartPhantomRedstoneRef(getSidedPos()))
        }
    }

    override fun onRemoved() {
        super.onRemoved()

        val world = getWorld()
        if (world is ServerWorld) {
            PhantomRedstone.removeRef(world, getTarget(), SidedPartPhantomRedstoneRef(getSidedPos()))
        }
    }

    override fun onUse(player: PlayerEntity, hand: Hand, hit: BlockHitResult): ActionResult {
        val superRes = super.onUse(player, hand, hit)
        if (superRes != ActionResult.PASS) return superRes

        return if (isClientSide()) {
            ActionResult.SUCCESS
        } else {
            val stack = player.getStackInHand(hand)

            if (player.isSneaking && stack.item == WRItems.PROJECTION_VIEWER) {
                ProjectionViewerUtil.setRef(stack, SidedPartPhantomRedstoneRef(getSidedPos()))
            } else {
                updateDistance((storedDistance + 1) % MAX_DISTANCE)
            }

            ActionResult.CONSUME
        }
    }

    fun calculateInputPower(): Int {
        val edge = RotationUtils.rotatedDirection(side, getInputSide())
        return getWorld().getEmittedRedstonePower(getPos().offset(edge), edge)
    }

    fun getInputSide(): Direction = direction.opposite

    fun getTarget(): BlockPos = getPos().offset(getOutputEdge(), blockDistance)

    fun getOutputEdge(): Direction = RotationUtils.rotatedDirection(side, direction)

    fun updateInputPower(power: Int) {
        val changed = inputPower != power
        val changedOutput = (inputPower == 0) != (power == 0)

        inputPower = power

        if (changed) {
            redraw()
            getBlockEntity().markDirty()
        }

        if (changedOutput) {
            val world = getWorld()
            val pos = getPos()
            world.updateNeighbor(getTarget().offset(getOutputEdge()), world.getBlockState(pos).block, pos)
        }
    }

    fun updateDistance(newDistance: Int) {
        val oldBlockDistance = blockDistance
        val changed = storedDistance != newDistance
        storedDistance = newDistance

        val world = getWorld()
        if (changed && world is ServerWorld) {
            redraw()
            getBlockEntity().markDirty()

            val pos = getPos()
            val sidedPos = getSidedPos()
            val outputEdge = getOutputEdge()
            val oldTarget = pos.offset(outputEdge, oldBlockDistance)
            val newTarget = getTarget()

            PhantomRedstone.removeRef(world, oldTarget, SidedPartPhantomRedstoneRef(sidedPos))
            PhantomRedstone.addRef(world, newTarget, SidedPartPhantomRedstoneRef(sidedPos))

            world.updateNeighbor(oldTarget.offset(outputEdge), world.getBlockState(pos).block, pos)
            world.updateNeighbor(newTarget.offset(outputEdge), world.getBlockState(pos).block, pos)
        }
    }
}
