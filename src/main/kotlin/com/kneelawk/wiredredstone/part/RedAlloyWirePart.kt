package com.kneelawk.wiredredstone.part

import alexiil.mc.lib.multipart.api.AbstractPart
import alexiil.mc.lib.multipart.api.MultipartHolder
import alexiil.mc.lib.multipart.api.PartDefinition
import alexiil.mc.lib.multipart.api.render.PartModelKey
import alexiil.mc.lib.net.IMsgReadCtx
import alexiil.mc.lib.net.IMsgWriteCtx
import alexiil.mc.lib.net.NetByteBuf
import com.kneelawk.wiredredstone.part.key.RedAlloyWirePartKey
import com.kneelawk.wiredredstone.util.RotationUtils
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import java.util.*

class RedAlloyWirePart(definition: PartDefinition, holder: MultipartHolder, val side: Direction) :
    AbstractPart(definition, holder) {

    companion object {
        private val CONFLICT_SHAPES = EnumMap<Direction, VoxelShape>(Direction::class.java)

        init {
            for (dir in Direction.values()) {
                CONFLICT_SHAPES[dir] = VoxelShapes.cuboid(
                    RotationUtils.rotatedBox(dir, Box(7.0 / 16.0, 0.0, 7.0 / 16.0, 9.0 / 16.0, 2.0 / 16.0, 9.0 / 16.0))
                )
            }
        }
    }

    constructor(definition: PartDefinition, holder: MultipartHolder, tag: NbtCompound) : this(
        definition, holder, Direction.byId(tag.getByte("side").toInt())
    )

    constructor(definition: PartDefinition, holder: MultipartHolder, buffer: NetByteBuf, ctx: IMsgReadCtx) : this(
        definition, holder, Direction.byId(buffer.readByte().toInt())
    )

    override fun toTag(): NbtCompound {
        val tag = super.toTag()
        tag.putByte("side", side.id.toByte())
        return tag
    }

    override fun writeCreationData(buffer: NetByteBuf, ctx: IMsgWriteCtx) {
        super.writeCreationData(buffer, ctx)
        buffer.writeByte(side.id)
    }

    override fun getShape(): VoxelShape {
        return CONFLICT_SHAPES[side]!!
    }

    override fun getCollisionShape(): VoxelShape {
        return VoxelShapes.empty()
    }

    override fun getModelKey(): PartModelKey {
        return RedAlloyWirePartKey(side)
    }

    override fun getClosestBlockState(): BlockState {
        return Blocks.REDSTONE_BLOCK.defaultState
    }

    override fun getCullingShape(): VoxelShape {
        return VoxelShapes.empty()
    }

    override fun getOutlineShape(): VoxelShape {
        return CONFLICT_SHAPES[side]!!
    }
}
