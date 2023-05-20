package com.kneelawk.wiredredstone.part

import alexiil.mc.lib.multipart.api.MultipartHolder
import alexiil.mc.lib.multipart.api.PartDefinition
import alexiil.mc.lib.multipart.api.render.PartModelKey
import alexiil.mc.lib.net.IMsgReadCtx
import alexiil.mc.lib.net.NetByteBuf
import com.kneelawk.graphlib.api.node.UniqueBlockNode
import com.kneelawk.wiredredstone.item.WRItems
import com.kneelawk.wiredredstone.node.GateDiodeBlockNode
import com.kneelawk.wiredredstone.part.key.GateDiodePartKey
import com.kneelawk.wiredredstone.util.LootTableUtil
import com.kneelawk.wiredredstone.util.getWorld
import net.minecraft.item.ItemStack
import net.minecraft.loot.context.LootContextParameterSet
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.Direction

class GateDiodePart : AbstractInputOutputGatePart {

    // Gates with more inputs might find it most efficient to just have a `shouldUpdate` variable, but since there is
    // only one input and one output and a pure function between them, we can always tell if we need an update just by
    // looking at the two of these.

    constructor(
        definition: PartDefinition, holder: MultipartHolder, side: Direction, connections: UByte, direction: Direction,
        inputPower: Int, outputPower: Int, outputReversePower: Int
    ) : super(definition, holder, side, connections, direction, inputPower, outputPower, outputReversePower)

    constructor(definition: PartDefinition, holder: MultipartHolder, tag: NbtCompound) : super(definition, holder, tag)
    constructor(definition: PartDefinition, holder: MultipartHolder, buffer: NetByteBuf, ctx: IMsgReadCtx) : super(
        definition, holder, buffer, ctx
    )

    override fun createBlockNodes(): Collection<UniqueBlockNode> {
        return listOf(GateDiodeBlockNode.Input(side), GateDiodeBlockNode.Output(side))
    }

    override fun shouldRecalculate(): Boolean {
        return outputPower != inputPower
    }

    override fun recalculate() {
        outputPower = inputPower
    }

    override fun getModelKey(): PartModelKey {
        return GateDiodePartKey(side, direction, connections, inputPower != 0, getTotalOutputPower() != 0)
    }

    override fun getPickStack(hit: BlockHitResult?): ItemStack {
        return ItemStack(WRItems.GATE_DIODE)
    }

    override fun addDrops(target: ItemDropTarget, params: LootContextParameterSet) {
        LootTableUtil.addPartDrops(getWorld(), target, params, WRParts.GATE_DIODE.identifier)
    }
}
