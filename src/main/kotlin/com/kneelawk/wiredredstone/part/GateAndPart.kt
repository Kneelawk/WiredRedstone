package com.kneelawk.wiredredstone.part

import alexiil.mc.lib.multipart.api.MultipartHolder
import alexiil.mc.lib.multipart.api.PartDefinition
import alexiil.mc.lib.multipart.api.render.PartModelKey
import alexiil.mc.lib.net.IMsgReadCtx
import alexiil.mc.lib.net.NetByteBuf
import com.kneelawk.graphlib.GraphLib
import com.kneelawk.graphlib.graph.BlockNode
import com.kneelawk.wiredredstone.item.WRItems
import com.kneelawk.wiredredstone.node.GateAndBlockNode
import com.kneelawk.wiredredstone.part.key.GateAndPartKey
import com.kneelawk.wiredredstone.util.BoundingBoxMap
import com.kneelawk.wiredredstone.util.LootTableUtil
import com.kneelawk.wiredredstone.util.PixelBox
import com.kneelawk.wiredredstone.util.getWorld
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.loot.context.LootContext
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import net.minecraft.util.shape.VoxelShape

class GateAndPart : AbstractThreeInputGatePart {
    companion object {
        private val SPECIAL_SHAPES = BoundingBoxMap.of(
            InputType.RIGHT to PixelBox(12, 0, 7, 15, 2, 10),
            InputType.BACK to PixelBox(6, 0, 10, 10, 2, 15),
            InputType.LEFT to PixelBox(1, 0, 7, 4, 2, 10)
        )
    }

    val enabledInputs = mutableSetOf(InputType.RIGHT, InputType.BACK, InputType.LEFT)

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
        for (input in enabledInputs) {
            nodes.add(GateAndBlockNode.Input(side, input))
        }
        nodes.add(GateAndBlockNode.Output(side))
        return nodes
    }

    override fun shouldRecalculate(): Boolean {
        return ((inputRightPower == 0) || (inputBackPower == 0) || (inputLeftPower == 0)) != (outputPower == 0)
    }

    override fun recalculate() {
        outputPower = if ((inputRightPower == 0) || (inputBackPower == 0) || (inputLeftPower == 0)) 0 else 15
    }

    override fun getModelKey(): PartModelKey {
        return GateAndPartKey(
            side, direction, connections, inputRightPower != 0, inputBackPower != 0, inputLeftPower != 0,
            outputPower != 0
        )
    }

    override fun getDynamicShape(partialTicks: Float, hitVec: Vec3d): VoxelShape {
        val hit = hitVec.subtract(Vec3d.of(getPos()))

        val touch = SPECIAL_SHAPES.getTouching(hit, orientation)
        if (touch != null) {
            return touch.shape
        }

        return super.getDynamicShape(partialTicks, hitVec)
    }

    override fun onUse(player: PlayerEntity, hand: Hand, hit: BlockHitResult): ActionResult {
        val hitVec = hit.pos.subtract(Vec3d.of(hit.blockPos))

        val touch = SPECIAL_SHAPES.getTouching(hitVec, orientation)
        if (touch != null) {
            val world = getWorld()
            if (world !is ServerWorld) {
                return ActionResult.SUCCESS
            }

            val input = touch.key

            if (enabledInputs.contains(input)) {
                enabledInputs.remove(input)
            } else {
                enabledInputs.add(input)
            }

            GraphLib.getController(world).updateNodes(getPos())

            return ActionResult.CONSUME
        }

        return ActionResult.PASS
    }

    override fun getPickStack(hitResult: BlockHitResult?): ItemStack {
        return ItemStack(WRItems.GATE_AND)
    }

    override fun addDrops(target: ItemDropTarget, context: LootContext) {
        LootTableUtil.addPartDrops(getWorld(), target, context, WRParts.GATE_AND.identifier)
    }
}
