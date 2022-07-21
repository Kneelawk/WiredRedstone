package com.kneelawk.wiredredstone.logic.phantom

import com.kneelawk.wiredredstone.WRConstants
import com.kneelawk.wiredredstone.WRLog
import com.kneelawk.wiredredstone.util.requireNonNull
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.world.PersistentState

class PhantomRedstoneStorage : PersistentState() {
    companion object {
        fun get(world: ServerWorld): PhantomRedstoneStorage = world.persistentStateManager.getOrCreate({ nbt ->
            val storage = PhantomRedstoneStorage()

            val list = nbt.getList("refs", NbtElement.COMPOUND_TYPE.toInt())
            for (elem in list) {
                val decode = decode(elem as? NbtCompound ?: continue) ?: continue
                storage.add(decode.pos, decode.ref)
            }

            storage
        }, ::PhantomRedstoneStorage, "${WRConstants.MOD_ID}_${WRConstants.PHANTOM_REDSTONE_NAME}")

        private fun encode(refObj: PhantomRedstoneRef, pos: BlockPos): NbtCompound {
            val ref = NbtCompound()

            ref.putInt("x", pos.x)
            ref.putInt("y", pos.y)
            ref.putInt("z", pos.z)

            ref.putString("type", refObj.id.toString())

            refObj.toTag()?.let { ref.put("data", it) }

            return ref
        }

        private fun decode(ref: NbtCompound): Decode? {
            val pos = BlockPos(ref.getInt("x"), ref.getInt("y"), ref.getInt("z"))

            val typeId = Identifier(ref.getString("type"))
            val decoder = PhantomRedstone.REF_DECODER_REGISTRY.get(typeId)

            if (decoder == null) {
                WRLog.warn("Tried to load unknown phantom redstone reference type: {} @ {}", typeId, pos)
                return null
            }

            val data = ref.get("data")
            val refObj = decoder.decode(data)

            if (refObj == null) {
                WRLog.warn("Failed to decode phantom redstone reference with type: {} @ {}", typeId, pos)
                return null
            }

            return Decode(refObj, pos)
        }
    }

    private val refs = Long2ObjectLinkedOpenHashMap<RefHolder>()

    override fun writeNbt(nbt: NbtCompound): NbtCompound {
        val list = NbtList()
        val iterator = refs.keys.longIterator()
        while (iterator.hasNext()) {
            val packedPos = iterator.nextLong()
            val holder = refs.get(packedPos) ?: continue

            val pos = BlockPos.fromLong(packedPos)

            when (holder) {
                is Single -> list.add(encode(holder.ref, pos))
                is Many -> for (refObj in holder.refs) {
                    list.add(encode(refObj, pos))
                }
            }
        }

        nbt.put("refs", list)

        return nbt
    }

    fun get(pos: BlockPos): RefHolder? {
        return refs.get(pos.asLong())
    }

    fun add(pos: BlockPos, ref: PhantomRedstoneRef) {
        val key = pos.asLong()

        if (refs.containsKey(key)) {
            when (val holder = refs.get(key).requireNonNull("Encountered null ref for valid key")) {
                is Many -> if (holder.refs.add(ref)) {
                    markDirty()
                }
                is Single -> if (holder.ref != ref) {
                    refs.put(key, Many(linkedSetOf(holder.ref, ref)))
                    markDirty()
                }
            }
        } else {
            refs.put(key, Single(ref))
            markDirty()
        }
    }

    fun remove(pos: BlockPos, ref: PhantomRedstoneRef): Boolean {
        val key = pos.asLong()

        return if (refs.containsKey(key)) {
            when (val holder = refs.get(key).requireNonNull("Encountered null ref for valid key")) {
                is Single -> {
                    if (holder.ref == ref) {
                        refs.remove(key)
                        markDirty()
                        true
                    } else false
                }
                is Many -> {
                    val removed = holder.refs.remove(ref)

                    if (holder.refs.isEmpty()) refs.remove(key)
                    else if (holder.refs.size == 1) refs.put(key, Single(holder.refs.first()))

                    if (removed) markDirty()
                    removed
                }
            }
        } else false
    }

    sealed interface RefHolder
    data class Single(val ref: PhantomRedstoneRef) : RefHolder
    data class Many(val refs: MutableSet<PhantomRedstoneRef>) : RefHolder

    private data class Decode(val ref: PhantomRedstoneRef, val pos: BlockPos)
}
