package com.kneelawk.wiredredstone.wirenet

import com.kneelawk.wiredredstone.WRLog
import com.kneelawk.wiredredstone.WRRegistries
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos

// This is almost completely copied from 2xsaiko's HCTM-Base.

data class NetworkPart<T : PartExt>(var pos: BlockPos, val ext: T) {
    fun toTag(tag: NbtCompound): NbtCompound {
        tag.putInt("x", pos.x)
        tag.putInt("y", pos.y)
        tag.putInt("z", pos.z)

        ext.toTag()?.let { tag.put("ext", it) }

        val typeId = WRRegistries.EXT_PART_TYPE.getId(ext.type)
        // This would be a programmer error, so better make a loud noise!
            ?: throw IllegalStateException("Attempted to store unknown PartExtType: ${ext.type}")

        tag.putString("type", typeId.toString())

        return tag
    }

    companion object {
        fun fromTag(tag: NbtCompound): NetworkPart<PartExt>? {
            val typeId = Identifier(tag.getString("type"))
            val type = WRRegistries.EXT_PART_TYPE[typeId]
            if (type == null) {
                WRLog.warn("Tried to load unknown PartExtType: $typeId")
                return null
            }

            val pos = BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z"))

            val extTag = tag["ext"]
            val ext = type.createExtFromTag(extTag)

            if (ext == null) {
                WRLog.warn("Unable to decode wirenet part with type: $typeId")
                return null
            }

            return NetworkPart(pos, ext)
        }
    }
}
