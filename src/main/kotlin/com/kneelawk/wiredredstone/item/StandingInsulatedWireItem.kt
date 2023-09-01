package com.kneelawk.wiredredstone.item

import com.kneelawk.wiredredstone.part.StandingInsulatedWirePart
import com.kneelawk.wiredredstone.part.WRParts
import com.kneelawk.wiredredstone.util.PlacementUtils
import net.minecraft.block.Blocks
import net.minecraft.item.Item
import net.minecraft.item.ItemUsageContext
import net.minecraft.util.ActionResult
import net.minecraft.util.DyeColor

class StandingInsulatedWireItem(private val color: DyeColor, settings: Settings) : Item(settings) {
    override fun useOnBlock(context: ItemUsageContext): ActionResult {
        if (context.world.isClient) {
            return ActionResult.CONSUME
        }

        val offer = PlacementUtils.tryPlaceCenterWire(context) {
            StandingInsulatedWirePart(WRParts.STANDING_INSULATED_WIRE, it, 0u, 0u, 0, color)
        } ?: return ActionResult.FAIL

        PlacementUtils.finishPlacement(context, offer, Blocks.WHITE_WOOL.defaultState)

        return ActionResult.SUCCESS
    }
}
