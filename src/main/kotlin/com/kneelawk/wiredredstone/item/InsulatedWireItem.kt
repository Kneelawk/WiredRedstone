package com.kneelawk.wiredredstone.item

import alexiil.mc.lib.multipart.api.MultipartHolder
import com.kneelawk.wiredredstone.part.InsulatedWirePart
import com.kneelawk.wiredredstone.part.WRParts
import com.kneelawk.wiredredstone.util.DyeColorUtil
import com.kneelawk.wiredredstone.util.PlacementUtils
import com.kneelawk.wiredredstone.util.bits.BlockageUtils
import net.minecraft.item.Item
import net.minecraft.item.ItemUsageContext
import net.minecraft.util.ActionResult
import net.minecraft.util.DyeColor
import net.minecraft.util.math.Direction

class InsulatedWireItem(private val color: DyeColor, settings: Settings) : Item(settings) {
    override fun useOnBlock(context: ItemUsageContext): ActionResult {
        val world = context.world
        if (world.isClient) {
            return ActionResult.PASS
        }

        val offer = PlacementUtils.tryPlaceWire(context, ::creator) ?: return ActionResult.FAIL

        PlacementUtils.finishPlacement(context, offer, DyeColorUtil.wool(color).defaultState)

        return ActionResult.SUCCESS
    }

    private fun creator(side: Direction): ((MultipartHolder) -> InsulatedWirePart) {
        return { holder ->
            InsulatedWirePart(WRParts.INSULATED_WIRE, holder, side, 0u, 0, BlockageUtils.UNBLOCKED, color)
        }
    }
}
