package com.kneelawk.wiredredstone.item

import com.kneelawk.wiredredstone.WRConstants
import net.minecraft.client.item.TooltipContext
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsageContext
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Formatting
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.world.World

open class ToolItem(settings: Settings, private val tooltipPath: String? = null, private val tooltipLines: Int = 1) : Item(settings) {
    override fun useOnBlock(context: ItemUsageContext): ActionResult {
        return context.world.getBlockState(context.blockPos).onUse(
            context.world, context.player, context.hand,
            BlockHitResult(context.hitPos, context.side, context.blockPos, context.hitsInsideBlock())
        )
    }

    override fun appendTooltip(
        stack: ItemStack, world: World?, tooltip: MutableList<Text>, context: TooltipContext
    ) {
        if (tooltipPath != null) {
            for (i in 0 until tooltipLines) {
                val path = if (i == 0) tooltipPath else "$tooltipPath.$i"
                tooltip.add(
                    WRConstants.tooltip(path)
                        .styled { it.withColor(Formatting.GRAY).withItalic(true) })
            }
        }
    }
}
