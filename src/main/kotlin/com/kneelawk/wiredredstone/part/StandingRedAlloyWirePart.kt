package com.kneelawk.wiredredstone.part

import alexiil.mc.lib.multipart.api.MultipartHolder
import alexiil.mc.lib.multipart.api.PartDefinition
import alexiil.mc.lib.multipart.api.render.PartModelKey
import alexiil.mc.lib.net.IMsgReadCtx
import alexiil.mc.lib.net.NetByteBuf
import com.kneelawk.graphlib.api.graph.user.BlockNode
import com.kneelawk.wiredredstone.item.WRItems
import com.kneelawk.wiredredstone.node.StandingRedAlloyBlockNode
import com.kneelawk.wiredredstone.part.key.StandingRedAlloyWirePartKey
import com.kneelawk.wiredredstone.util.BoundingBoxUtils
import com.kneelawk.wiredredstone.util.LootTableUtil
import com.kneelawk.wiredredstone.util.PixelBox
import com.kneelawk.wiredredstone.util.vs
import net.minecraft.item.ItemStack
import net.minecraft.loot.context.LootContextParameterSet
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.shape.VoxelShape

class StandingRedAlloyWirePart : AbstractCenterRedstoneWirePart, CenterPart {

    companion object {
        const val WIRE_DIAMETER = 2.0

        private val CONFLICT_SHAPE = PixelBox(
            8.0 - WIRE_DIAMETER / 2.0, 8.0 - WIRE_DIAMETER / 2.0, 8.0 - WIRE_DIAMETER / 2.0, 8.0 + WIRE_DIAMETER / 2.0,
            8.0 + WIRE_DIAMETER / 2.0, 8.0 + WIRE_DIAMETER / 2.0
        ).vs()
        private val OUTLINE_SHAPES = BoundingBoxUtils.getCenterWireOutlineShapes(WIRE_DIAMETER + 2.0)
        private val COLLISION_SHAPES = BoundingBoxUtils.getCenterWireOutlineShapes(WIRE_DIAMETER)
    }

    constructor(
        definition: PartDefinition, holder: MultipartHolder, connections: UByte, blockage: UByte, power: Int
    ) : super(definition, holder, connections, blockage, power)

    constructor(definition: PartDefinition, holder: MultipartHolder, tag: NbtCompound) : super(definition, holder, tag)
    constructor(definition: PartDefinition, holder: MultipartHolder, buffer: NetByteBuf, ctx: IMsgReadCtx) : super(
        definition, holder, buffer, ctx
    )

    override val wireDiameter = WIRE_DIAMETER

    override fun getReceivingPower(): Int {
        // TODO
        return 0
    }

    override fun getShape(): VoxelShape {
        return CONFLICT_SHAPE
    }

    override fun getCollisionShape(): VoxelShape {
        return COLLISION_SHAPES[connections]
    }

    override fun getOutlineShape(): VoxelShape {
        return OUTLINE_SHAPES[connections]
    }

    override fun getModelKey(): PartModelKey {
        return StandingRedAlloyWirePartKey(connections, power != 0)
    }

    override fun createBlockNodes(): Collection<BlockNode> {
        return listOf(StandingRedAlloyBlockNode)
    }

    override fun getPickStack(hitResult: BlockHitResult?): ItemStack {
        return ItemStack(WRItems.STANDING_RED_ALLOY_WIRE)
    }

    override fun addDrops(target: ItemDropTarget, params: LootContextParameterSet) {
        LootTableUtil.addPartDrops(this, target, params, WRParts.STANDING_RED_ALLOY_WIRE.identifier)
    }
}
