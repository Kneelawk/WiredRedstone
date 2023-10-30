package com.kneelawk.wiredredstone.item

import com.kneelawk.wiredredstone.logic.phantom.PhantomRedstone
import com.kneelawk.wiredredstone.logic.phantom.PhantomRedstoneRef
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.Identifier

object ProjectionViewerUtil {
    private const val NBT_KEY = "wiredredstone:projection_viewer"

    fun setRef(stack: ItemStack, ref: PhantomRedstoneRef) {
        val nbt = NbtCompound()

        nbt.putString("type", ref.id.toString())
        ref.toTag()?.let { nbt.put("data", it) }

        stack.setSubNbt(NBT_KEY, nbt)
    }

    fun getRef(stack: ItemStack): PhantomRedstoneRef? {
        val nbt = stack.getSubNbt(NBT_KEY) ?: return null

        val typeId = Identifier(nbt.getString("type"))
        val decoder = PhantomRedstone.REF_DECODER_REGISTRY.get(typeId) ?: return null

        val data = nbt.get("data")

        return decoder.decode(data)
    }
}
