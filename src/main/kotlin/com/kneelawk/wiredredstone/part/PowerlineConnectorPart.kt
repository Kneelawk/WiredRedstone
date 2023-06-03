package com.kneelawk.wiredredstone.part

import alexiil.mc.lib.multipart.api.MultipartHolder
import alexiil.mc.lib.multipart.api.PartDefinition
import alexiil.mc.lib.multipart.api.render.PartModelKey
import alexiil.mc.lib.net.IMsgReadCtx
import alexiil.mc.lib.net.NetByteBuf
import com.kneelawk.graphlib.api.graph.user.BlockNode
import com.kneelawk.wiredredstone.item.WRItems
import com.kneelawk.wiredredstone.node.PowerlineConnectorBlockNode
import com.kneelawk.wiredredstone.part.key.PowerlineConnectorPartKey
import com.kneelawk.wiredredstone.util.BoundingBoxUtils
import com.kneelawk.wiredredstone.util.LootTableUtil
import com.kneelawk.wiredredstone.util.PixelBox
import com.kneelawk.wiredredstone.util.getWorld
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.loot.context.LootContextParameterSet
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape

class PowerlineConnectorPart : AbstractSidedPart {
    companion object {
        private val SHAPES = BoundingBoxUtils.getRotatedShapes(PixelBox(6, 5, 6, 10, 9, 10))
    }

    constructor(definition: PartDefinition, holder: MultipartHolder, side: Direction) : super(definition, holder, side)
    constructor(definition: PartDefinition, holder: MultipartHolder, tag: NbtCompound) : super(definition, holder, tag)
    constructor(definition: PartDefinition, holder: MultipartHolder, buffer: NetByteBuf, ctx: IMsgReadCtx) : super(
        definition, holder, buffer, ctx
    )

    override fun getShape(): VoxelShape = SHAPES[side]!!

    override fun getModelKey(): PartModelKey = PowerlineConnectorPartKey(side)

    override fun createBlockNodes(): Collection<BlockNode> = listOf(PowerlineConnectorBlockNode(side))

    override fun getClosestBlockState(): BlockState = Blocks.STONE.defaultState

    fun getBlockNode() = PowerlineConnectorBlockNode(side)

    override fun getPickStack(hitResult: BlockHitResult?): ItemStack = ItemStack(WRItems.POWERLINE_CONNECTOR)

    override fun addDrops(target: ItemDropTarget, params: LootContextParameterSet) {
        LootTableUtil.addPartDrops(getWorld(), target, params, WRParts.POWERLINE_CONNECTOR.identifier)
    }
}
