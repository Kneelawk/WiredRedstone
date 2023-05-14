package com.kneelawk.wiredredstone.part

import alexiil.mc.lib.multipart.api.MultipartHolder
import alexiil.mc.lib.multipart.api.PartDefinition
import alexiil.mc.lib.multipart.api.render.PartModelKey
import alexiil.mc.lib.net.IMsgReadCtx
import alexiil.mc.lib.net.NetByteBuf
import com.kneelawk.graphlib.api.node.BlockNode
import com.kneelawk.wiredredstone.item.WRItems
import com.kneelawk.wiredredstone.node.GateNotBlockNode
import com.kneelawk.wiredredstone.part.key.GateNotPartKey
import com.kneelawk.wiredredstone.util.LootTableUtil
import com.kneelawk.wiredredstone.util.getWorld
import net.minecraft.item.ItemStack
import net.minecraft.loot.context.LootContextParameterSet
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.Direction

class GateNotPart : AbstractInputOutputGatePart {
    constructor(
        definition: PartDefinition, holder: MultipartHolder, side: Direction, connections: UByte, direction: Direction,
        inputPower: Int, outputPower: Int, outputReversePower: Int
    ) : super(definition, holder, side, connections, direction, inputPower, outputPower, outputReversePower)

    constructor(definition: PartDefinition, holder: MultipartHolder, tag: NbtCompound) : super(definition, holder, tag)
    constructor(definition: PartDefinition, holder: MultipartHolder, buffer: NetByteBuf, ctx: IMsgReadCtx) : super(
        definition, holder, buffer, ctx
    )

    override fun createBlockNodes(): Collection<BlockNode> {
        return listOf(GateNotBlockNode.Input(side), GateNotBlockNode.Output(side))
    }

    override fun shouldRecalculate(): Boolean {
        return (inputPower == 0) == (outputPower == 0)
    }

    override fun recalculate() {
        outputPower = if (inputPower == 0) 15 else 0
    }

    override fun getModelKey(): PartModelKey {
        return GateNotPartKey(
            side, direction, connections, inputPower != 0, outputPower != 0, getTotalOutputPower() != 0
        )
    }

    override fun getPickStack(hitResult: BlockHitResult?): ItemStack {
        return ItemStack(WRItems.GATE_NOT)
    }

    override fun addDrops(target: ItemDropTarget, params: LootContextParameterSet) {
        LootTableUtil.addPartDrops(getWorld(), target, params, WRParts.GATE_NOT.identifier)
    }
}
