package com.kneelawk.wiredredstone.logic.phantom

import com.kneelawk.graphlib.util.SidedPos
import com.kneelawk.wiredredstone.part.PhantomRedstoneProviderPart
import com.kneelawk.wiredredstone.part.SidedPart
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction

data class SidedPartPhantomRedstoneRef(val pos: SidedPos) : PhantomRedstoneRef {
    override val id = PhantomRedstone.SIDED_PART_ID

    override fun toTag(): NbtElement {
        val tag = NbtCompound()
        tag.putByte("side", pos.side.id.toByte())
        tag.putInt("x", pos.pos.x)
        tag.putInt("y", pos.pos.y)
        tag.putInt("z", pos.pos.z)
        return tag
    }

    override fun getStrongRedstonePower(
        original: Int, world: ServerWorld, pos: BlockPos, oppositeFace: Direction
    ): PhantomRedstoneRef.Lookup {
        val part =
            SidedPart.getPart(world, this.pos) as? PhantomRedstoneProviderPart ?: return PhantomRedstoneRef.NotFound
        return PhantomRedstoneRef.Found(part.getStrongRedstonePower(original, world, pos, oppositeFace))
    }

    override fun getWeakRedstonePower(
        original: Int, world: ServerWorld, pos: BlockPos, oppositeFace: Direction
    ): PhantomRedstoneRef.Lookup {
        val part =
            SidedPart.getPart(world, this.pos) as? PhantomRedstoneProviderPart ?: return PhantomRedstoneRef.NotFound
        return PhantomRedstoneRef.Found(part.getWeakRedstonePower(original, world, pos, oppositeFace))
    }

    object Decoder : PhantomRedstoneRefDecoder {
        override fun decode(nbt: NbtElement?): PhantomRedstoneRef? {
            val tag = nbt as? NbtCompound ?: return null

            val side = if (tag.contains("side", NbtElement.NUMBER_TYPE.toInt())) Direction.byId(
                tag.getByte("side").toInt()
            ) else return null
            val x = if (tag.contains("x", NbtElement.NUMBER_TYPE.toInt())) tag.getInt("x") else return null
            val y = if (tag.contains("y", NbtElement.NUMBER_TYPE.toInt())) tag.getInt("y") else return null
            val z = if (tag.contains("z", NbtElement.NUMBER_TYPE.toInt())) tag.getInt("z") else return null

            return SidedPartPhantomRedstoneRef(SidedPos(BlockPos(x, y, z), side))
        }
    }
}
