package com.kneelawk.wiredredstone.part

import alexiil.mc.lib.multipart.api.MultipartHolder
import alexiil.mc.lib.multipart.api.PartDefinition
import alexiil.mc.lib.multipart.api.render.PartModelKey
import alexiil.mc.lib.net.IMsgReadCtx
import alexiil.mc.lib.net.NetByteBuf
import com.kneelawk.graphlib.api.graph.user.BlockNode
import com.kneelawk.wiredredstone.item.WRItems
import com.kneelawk.wiredredstone.node.GateXnorBlockNode
import com.kneelawk.wiredredstone.part.key.GateXnorPartKey
import com.kneelawk.wiredredstone.util.LootTableUtil
import net.minecraft.item.ItemStack
import net.minecraft.loot.context.LootContextParameterSet
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.Direction

class GateXnorPart : AbstractTwoInputGatePart {
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
        val nodes = mutableListOf<BlockNode>()
        for (input in InputType.values()) {
            nodes.add(GateXnorBlockNode.Input(side, input))
        }
        nodes.add(GateXnorBlockNode.Output(side))
        return nodes
    }

    override fun shouldRecalculate(): Boolean {
        return ((inputLeftPower != 0) xor (inputRightPower != 0)) == (outputPower != 0)
    }

    override fun recalculate() {
        outputPower = if ((inputLeftPower != 0) xor (inputRightPower != 0)) 0 else 15
    }

    override fun getModelKey(): PartModelKey {
        return GateXnorPartKey(
            side, direction, connections, inputRightPower != 0, inputLeftPower != 0, outputPower != 0
        )
    }

    override fun getPickStack(hitResult: BlockHitResult?): ItemStack {
        return ItemStack(WRItems.GATE_XNOR)
    }

    override fun addDrops(target: ItemDropTarget, context: LootContextParameterSet) {
        LootTableUtil.addPartDrops(this, target, context, WRParts.GATE_XNOR.identifier)
    }
}
