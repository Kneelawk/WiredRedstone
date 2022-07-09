package com.kneelawk.wiredredstone.part

import alexiil.mc.lib.multipart.api.MultipartHolder
import alexiil.mc.lib.multipart.api.PartDefinition
import alexiil.mc.lib.multipart.api.render.PartModelKey
import alexiil.mc.lib.net.IMsgReadCtx
import alexiil.mc.lib.net.NetByteBuf
import com.kneelawk.graphlib.graph.BlockNode
import com.kneelawk.wiredredstone.item.WRItems
import com.kneelawk.wiredredstone.node.GateNorBlockNode
import com.kneelawk.wiredredstone.part.key.GateNorPartKey
import com.kneelawk.wiredredstone.util.LootTableUtil
import com.kneelawk.wiredredstone.util.getWorld
import net.minecraft.item.ItemStack
import net.minecraft.loot.context.LootContext
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.Direction

class GateNorPart : AbstractThreeInputGatePart {
    constructor(
        definition: PartDefinition, holder: MultipartHolder, side: Direction, connections: UByte, direction: Direction,
        inputRightPower: Int, inputBackPower: Int, inputLeftPower: Int, outputPower: Int, outputReversePower: Int
    ) : super(
        definition, holder, side, connections, direction, inputRightPower, inputBackPower, inputLeftPower, outputPower,
        outputReversePower
    )

    constructor(definition: PartDefinition, holder: MultipartHolder, tag: NbtCompound) : super(definition, holder, tag)
    constructor(definition: PartDefinition, holder: MultipartHolder, buffer: NetByteBuf, ctx: IMsgReadCtx) : super(
        definition, holder, buffer, ctx
    )

    override fun createBlockNodes(): Collection<BlockNode> {
        return listOf(
            GateNorBlockNode.Input(side, GateNorBlockNode.InputType.RIGHT),
            GateNorBlockNode.Input(side, GateNorBlockNode.InputType.BACK),
            GateNorBlockNode.Input(side, GateNorBlockNode.InputType.LEFT),
            GateNorBlockNode.Output(side)
        )
    }

    override fun shouldRecalculate(): Boolean {
        return ((inputRightPower == 0) && (inputBackPower == 0) && (inputLeftPower == 0)) == (outputPower == 0)
    }

    override fun recalculate() {
        outputPower = if ((inputRightPower == 0) && (inputBackPower == 0) && (inputLeftPower == 0)) 15 else 0
    }

    override fun getModelKey(): PartModelKey {
        return GateNorPartKey(
            side, direction, connections, inputRightPower != 0, inputBackPower != 0, inputLeftPower != 0,
            outputPower != 0, getTotalOutputPower() != 0
        )
    }

    override fun getPickStack(hitResult: BlockHitResult?): ItemStack {
        return ItemStack(WRItems.GATE_NOR)
    }

    override fun addDrops(target: ItemDropTarget, context: LootContext) {
        LootTableUtil.addPartDrops(getWorld(), target, context, WRParts.GATE_NOR.identifier)
    }
}
