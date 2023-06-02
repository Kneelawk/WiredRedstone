package com.kneelawk.wiredredstone.item

import alexiil.mc.lib.multipart.api.MultipartUtil
import com.kneelawk.wiredredstone.part.PowerlineConnectorPart
import com.kneelawk.wiredredstone.part.WRParts
import com.kneelawk.wiredredstone.util.PlacementUtils
import net.minecraft.block.Blocks
import net.minecraft.item.Item
import net.minecraft.item.ItemUsageContext
import net.minecraft.util.ActionResult

class PowerlineConnectorItem(settings: Settings) : Item(settings) {
    override fun useOnBlock(context: ItemUsageContext): ActionResult {
        val world = context.world
        val pos = context.blockPos
        val side = context.side

        if (world.isClient) return ActionResult.CONSUME

        val part = MultipartUtil.offerNewPart(world, pos) {
            PowerlineConnectorPart(WRParts.POWERLINE_CONNECTOR, it, side.opposite)
        }
        if (part != null) {
            PlacementUtils.finishPlacement(context, part, Blocks.STONE.defaultState)
            return ActionResult.SUCCESS
        }

        val part2 = MultipartUtil.offerNewPart(world, pos.offset(side)) {
            PowerlineConnectorPart(WRParts.POWERLINE_CONNECTOR, it, side.opposite)
        }
        if (part2 != null) {
            PlacementUtils.finishPlacement(context, part2, Blocks.STONE.defaultState)
            return ActionResult.SUCCESS
        }

        return ActionResult.FAIL
    }
}
