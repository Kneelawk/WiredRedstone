package com.kneelawk.wiredredstone.item

import com.kneelawk.wiredredstone.WRConstants.tooltip
import com.kneelawk.wiredredstone.logic.phantom.PhantomRedstone
import com.kneelawk.wiredredstone.logic.phantom.PhantomRedstoneRef
import net.minecraft.client.item.TooltipContext
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsageContext
import net.minecraft.nbt.NbtCompound
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.world.World

class ProjectionViewerItem(settings: Settings) : Item(settings) {
    companion object {
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

    override fun useOnBlock(context: ItemUsageContext): ActionResult {
        return context.world.getBlockState(context.blockPos).onUse(
            context.world, context.player, context.hand,
            BlockHitResult(context.hitPos, context.side, context.blockPos, context.hitsInsideBlock())
        )
    }

    override fun appendTooltip(
        stack: ItemStack, world: World?, tooltip: MutableList<Text>, context: TooltipContext
    ) {
        tooltip.add(tooltip("projection_viewer.description").styled { it.withColor(Formatting.GRAY).withItalic(true) })
    }
}
