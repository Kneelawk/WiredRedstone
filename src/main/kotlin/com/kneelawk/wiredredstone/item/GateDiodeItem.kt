package com.kneelawk.wiredredstone.item

import alexiil.mc.lib.multipart.api.MultipartHolder
import com.kneelawk.wiredredstone.part.GateDiodePart
import com.kneelawk.wiredredstone.part.WRParts
import com.kneelawk.wiredredstone.util.PlacementUtils
import net.minecraft.block.Blocks
import net.minecraft.item.Item
import net.minecraft.item.ItemUsageContext
import net.minecraft.util.ActionResult
import net.minecraft.util.math.Direction

class GateDiodeItem(settings: Settings) : Item(settings), GateItem {
    override fun useOnBlock(context: ItemUsageContext): ActionResult {
        val world = context.world
        if (world.isClient) {
            return ActionResult.PASS
        }

        val offer = PlacementUtils.tryPlaceGate(context, ::creator) ?: return ActionResult.FAIL

        PlacementUtils.finishPlacement(context, offer, Blocks.REPEATER.defaultState)

        return ActionResult.SUCCESS
    }

    private fun creator(side: Direction, direction: Direction): (MultipartHolder) -> GateDiodePart {
        return { holder ->
            GateDiodePart(WRParts.GATE_DIODE, holder, side, 0u, direction, 0, 0, 0)
        }
    }
}