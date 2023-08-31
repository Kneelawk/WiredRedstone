package com.kneelawk.wiredredstone.item

import com.kneelawk.wiredredstone.part.StandingRedAlloyWirePart
import com.kneelawk.wiredredstone.part.WRParts
import com.kneelawk.wiredredstone.util.PlacementUtils
import net.minecraft.block.Blocks
import net.minecraft.item.Item
import net.minecraft.item.ItemUsageContext
import net.minecraft.util.ActionResult

class StandingRedAlloyWireItem(settings: Settings) : Item(settings) {
    override fun useOnBlock(context: ItemUsageContext): ActionResult {
        if (context.world.isClient) {
            return ActionResult.CONSUME
        }

        val offer = PlacementUtils.tryPlaceCenterWire(context) {
            StandingRedAlloyWirePart(WRParts.STANDING_RED_ALLOY_WIRE, it, 0u, 0u, 0)
        } ?: return ActionResult.FAIL

        PlacementUtils.finishPlacement(context, offer, Blocks.REDSTONE_BLOCK.defaultState)

        return ActionResult.SUCCESS
    }
}
