package com.kneelawk.wiredredstone.logic.phantom

import com.kneelawk.graphlib.world.StorageChunk
import com.kneelawk.wiredredstone.WRLog
import it.unimi.dsi.fastutil.shorts.Short2ObjectLinkedOpenHashMap
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkSectionPos

class PhantomRedstoneChunk : StorageChunk {
    private val pos: ChunkSectionPos
    private val refs = Short2ObjectLinkedOpenHashMap<MutableSet<PhantomRedstoneRef>>()

    constructor(pos: ChunkSectionPos) {
        this.pos = pos
    }

    constructor(nbt: NbtCompound, pos: ChunkSectionPos) {
        this.pos = pos

        val list = nbt.getList("refs", NbtElement.COMPOUND_TYPE.toInt())
        for (elem in list) {
            val ref = elem as NbtCompound
            val localX = ref.getByte("x").toInt()
            val localY = ref.getByte("y").toInt()
            val localZ = ref.getByte("z").toInt()
            val localCoord = ChunkSectionPos.packLocal(BlockPos(localX, localY, localZ))

            val typeId = Identifier(ref.getString("type"))
            val decoder = PhantomRedstone.REF_DECODER_REGISTRY.get(typeId)

            if (decoder == null) {
                val blockPos = pos.unpackBlockPos(localCoord)
                WRLog.warn("Tried to load unknown phantom redstone reference type: {} @ {}", typeId, blockPos)
                continue
            }

            val data = ref.get("data")
            val refObj = decoder.decode(data)

            if (refObj == null) {
                val blockPos = pos.unpackBlockPos(localCoord)
                WRLog.warn("Failed to decode phantom redstone reference with type: {} @ {}", typeId, blockPos)
                continue
            }

            if (refs.containsKey(localCoord)) {
                refs.get(localCoord).add(refObj)
            } else {
                val set = linkedSetOf<PhantomRedstoneRef>()
                set.add(refObj)
                refs.put(localCoord, set)
            }
        }
    }

    override fun toNbt(nbt: NbtCompound) {
        val list = NbtList()
        val iterator = refs.keys.intIterator()
        while (iterator.hasNext()) {
            val localCoord = iterator.nextInt().toShort()
            val set = refs.get(localCoord)

            for (refObj in set) {
                val ref = NbtCompound()

                ref.putByte("x", ChunkSectionPos.unpackLocalX(localCoord).toByte())
                ref.putByte("y", ChunkSectionPos.unpackLocalY(localCoord).toByte())
                ref.putByte("z", ChunkSectionPos.unpackLocalZ(localCoord).toByte())

                ref.putString("type", refObj.id.toString())

                refObj.toTag()?.let { ref.put("data", it) }

                list.add(ref)
            }
        }

        nbt.put("refs", list)
    }

    fun get(pos: BlockPos): Set<PhantomRedstoneRef> = refs.get(ChunkSectionPos.packLocal(pos))

    fun add(pos: BlockPos, ref: PhantomRedstoneRef) {
        val localCoord = ChunkSectionPos.packLocal(pos)
        if (refs.containsKey(localCoord)) {
            refs.get(localCoord).add(ref)
        } else {
            val set = linkedSetOf<PhantomRedstoneRef>()
            set.add(ref)
            refs.put(localCoord, set)
        }
    }

    fun remove(pos: BlockPos, ref: PhantomRedstoneRef): Boolean {
        val localCoord = ChunkSectionPos.packLocal(pos)

        if (refs.containsKey(localCoord)) {
            val set = refs.get(localCoord)

            val removed = set.remove(ref)

            if (set.isEmpty()) {
                refs.remove(localCoord)
            }

            return removed
        }

        return false
    }
}
