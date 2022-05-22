package com.kneelawk.wiredredstone.part

import alexiil.mc.lib.multipart.api.MultipartHolder
import alexiil.mc.lib.multipart.api.PartDefinition
import alexiil.mc.lib.multipart.api.render.PartModelKey
import alexiil.mc.lib.net.IMsgReadCtx
import alexiil.mc.lib.net.IMsgWriteCtx
import alexiil.mc.lib.net.NetByteBuf
import com.kneelawk.graphlib.graph.BlockNode
import com.kneelawk.wiredredstone.item.WRItems
import com.kneelawk.wiredredstone.node.GateRepeaterBlockNode
import com.kneelawk.wiredredstone.part.key.GateRepeaterPartKey
import com.kneelawk.wiredredstone.util.LootTableUtil
import com.kneelawk.wiredredstone.util.getWorld
import com.kneelawk.wiredredstone.util.isClientSide
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.loot.context.LootContext
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.Direction

class GateRepeaterPart : AbstractInputOutputGatePart {

    companion object {
        private const val MAX_DELAY = 20
    }

    /**
     * A delay of 0 corresponds to 0.5 redstone ticks, 1 to 1 redstone tick, 2 to 1.5 redstone  ticks, 3 to 2 redstone
     * ticks, etc.
     *
     * **Equation:**
     * ```
     * (delay + 1) / 2
     * ```
     */
    var delay: Int
        private set
    var timer: Int
        private set
    var storedInput: Boolean
        private set

    constructor(
        definition: PartDefinition, holder: MultipartHolder, side: Direction, connections: UByte, direction: Direction,
        inputPower: Int, outputPower: Int, outputReversePower: Int, delay: Int, timer: Int, storedInput: Boolean
    ) : super(definition, holder, side, connections, direction, inputPower, outputPower, outputReversePower) {
        this.delay = delay
        this.timer = timer
        this.storedInput = storedInput
    }

    constructor(definition: PartDefinition, holder: MultipartHolder, tag: NbtCompound) : super(
        definition, holder, tag
    ) {
        delay = tag.getByte("delay").toInt().coerceIn(0, MAX_DELAY)
        timer = tag.getByte("timer").toInt().coerceIn(0, MAX_DELAY)
        storedInput = tag.getBoolean("storedInput")
    }

    constructor(definition: PartDefinition, holder: MultipartHolder, buffer: NetByteBuf, ctx: IMsgReadCtx) : super(
        definition, holder, buffer, ctx
    ) {
        delay = buffer.readFixedBits(5).coerceIn(0, MAX_DELAY)
        timer = 0
        storedInput = false
    }

    override fun toTag(): NbtCompound {
        val tag = super.toTag()
        tag.putByte("delay", delay.toByte())
        tag.putByte("timer", timer.toByte())
        tag.putBoolean("storedInput", storedInput)
        return tag
    }

    override fun writeCreationData(buffer: NetByteBuf, ctx: IMsgWriteCtx) {
        super.writeCreationData(buffer, ctx)
        buffer.writeFixedBits(delay, 5)
    }

    override fun writeRenderData(buffer: NetByteBuf, ctx: IMsgWriteCtx) {
        super.writeRenderData(buffer, ctx)
        buffer.writeFixedBits(delay, 5)
    }

    override fun readRenderData(buffer: NetByteBuf, ctx: IMsgReadCtx) {
        super.readRenderData(buffer, ctx)
        delay = buffer.readFixedBits(5).coerceIn(0, MAX_DELAY)
    }

    override fun createBlockNodes(): Collection<BlockNode> {
        return listOf(GateRepeaterBlockNode.Input(side), GateRepeaterBlockNode.Output(side))
    }

    override fun shouldRecalculate(): Boolean {
        return if (timer < 1) {
            storedInput != (outputPower != 0)
        } else {
            timer--
            false
        }
    }

    override fun recalculate() {
        outputPower = if (storedInput) 15 else 0
    }

    override fun getModelKey(): PartModelKey {
        return GateRepeaterPartKey(side, direction, connections, delay, inputPower != 0, outputPower != 0)
    }

    override fun updateInputPower(power: Int) {
        super.updateInputPower(power)

        if (timer < 1 && storedInput != (power != 0)) {
            storedInput = power != 0
            timer = delay
        }
    }

    override fun getPickStack(hitResult: BlockHitResult?): ItemStack {
        return ItemStack(WRItems.GATE_REPEATER)
    }

    override fun addDrops(target: ItemDropTarget, context: LootContext) {
        LootTableUtil.addPartDrops(getWorld(), target, context, WRParts.GATE_REPEATER.identifier)
    }

    override fun onUse(player: PlayerEntity?, hand: Hand?, hit: BlockHitResult?): ActionResult {
        return if (isClientSide()) {
            ActionResult.SUCCESS
        } else {
            delay = (delay + 1) % (MAX_DELAY + 1)
            redraw()
            ActionResult.CONSUME
        }
    }
}
