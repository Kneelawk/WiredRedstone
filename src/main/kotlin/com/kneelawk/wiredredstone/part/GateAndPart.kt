package com.kneelawk.wiredredstone.part

import alexiil.mc.lib.multipart.api.MultipartHolder
import alexiil.mc.lib.multipart.api.PartDefinition
import alexiil.mc.lib.multipart.api.render.PartModelKey
import alexiil.mc.lib.net.IMsgReadCtx
import alexiil.mc.lib.net.NetByteBuf
import com.kneelawk.graphlib.api.node.BlockNode
import com.kneelawk.wiredredstone.item.WRItems
import com.kneelawk.wiredredstone.node.GateAndBlockNode
import com.kneelawk.wiredredstone.part.key.GateAndPartKey
import com.kneelawk.wiredredstone.util.*
import net.minecraft.item.ItemStack
import net.minecraft.loot.context.LootContextParameterSet
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.Direction

class GateAndPart : AbstractDisableableThreeInputGatePart {
    companion object {
        private val INPUT_SHAPES = BoundingBoxMap.ofVoxelShapes(
            InputType.RIGHT to PixelBox(12, 0, 7, 15, 2, 10).vs(),
            InputType.BACK to PixelBox(7, 0, 10, 10, 2, 12).vs()
                .union(PixelBox(6, 0, 12, 9, 2, 15).vs()),
            InputType.LEFT to PixelBox(1, 0, 7, 4, 2, 10).vs()
        )
    }

    constructor(
        definition: PartDefinition, holder: MultipartHolder, side: Direction, connections: UByte, direction: Direction,
        inputRightPower: Int, inputBackPower: Int, inputLeftPower: Int, outputPower: Int, outputReversePower: Int,
        inputRightEnabled: Boolean, inputBackEnabled: Boolean, inputLeftEnabled: Boolean
    ) : super(
        definition, holder, side, connections, direction, inputRightPower, inputBackPower, inputLeftPower, outputPower,
        outputReversePower, inputRightEnabled, inputBackEnabled, inputLeftEnabled
    )

    constructor(definition: PartDefinition, holder: MultipartHolder, tag: NbtCompound) : super(definition, holder, tag)
    constructor(definition: PartDefinition, holder: MultipartHolder, buffer: NetByteBuf, ctx: IMsgReadCtx) : super(
        definition, holder, buffer, ctx
    )

    override val inputShapes = INPUT_SHAPES

    override fun createBlockNodes(): Collection<BlockNode> {
        val nodes = mutableListOf<BlockNode>()
        for (input in enabledInputs) {
            nodes.add(GateAndBlockNode.Input(side, input))
        }
        nodes.add(GateAndBlockNode.Output(side))
        return nodes
    }

    override fun shouldRecalculate(): Boolean {
        return ((inputRightPower == 0 && inputRightEnabled) ||
                (inputBackPower == 0 && inputBackEnabled) ||
                (inputLeftPower == 0 && inputLeftEnabled)) != (outputPower == 0)
    }

    override fun recalculate() {
        outputPower = if ((inputRightPower == 0 && inputRightEnabled) ||
            (inputBackPower == 0 && inputBackEnabled) ||
            (inputLeftPower == 0 && inputLeftEnabled)
        ) 0 else 15
    }

    override fun getModelKey(): PartModelKey {
        return GateAndPartKey(
            side, direction, connections, inputRightPower != 0, inputBackPower != 0, inputLeftPower != 0,
            outputPower != 0, inputRightEnabled, inputBackEnabled, inputLeftEnabled
        )
    }

    override fun getPickStack(hitResult: BlockHitResult?): ItemStack {
        return ItemStack(WRItems.GATE_AND)
    }

    override fun addDrops(target: ItemDropTarget, context: LootContextParameterSet) {
        LootTableUtil.addPartDrops(getWorld(), target, context, WRParts.GATE_AND.identifier)
    }
}
