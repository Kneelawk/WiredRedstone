package com.kneelawk.wiredredstone.item

import alexiil.mc.lib.multipart.api.MultipartHolder
import com.kneelawk.wiredredstone.part.BundledCablePart
import com.kneelawk.wiredredstone.part.WRParts
import com.kneelawk.wiredredstone.util.DyeColorUtil
import com.kneelawk.wiredredstone.util.PlacementUtils
import com.kneelawk.wiredredstone.util.bits.BlockageUtils
import net.minecraft.block.Blocks
import net.minecraft.item.Item
import net.minecraft.item.ItemUsageContext
import net.minecraft.util.ActionResult
import net.minecraft.util.DyeColor
import net.minecraft.util.math.Direction

class BundledCableItem(private val color: DyeColor?, settings: Settings) : Item(settings) {
    override fun useOnBlock(context: ItemUsageContext): ActionResult {
        val world = context.world
        if (world.isClient) {
            return ActionResult.PASS
        }

        val offer = PlacementUtils.tryPlaceSidedWire(context, ::creator) ?: return ActionResult.FAIL

        PlacementUtils.finishPlacement(
            context, offer, (color?.let(DyeColorUtil::wool) ?: Blocks.WHITE_WOOL).defaultState
        )

        return ActionResult.SUCCESS
    }

    private fun creator(side: Direction): ((MultipartHolder) -> BundledCablePart) {
        return { holder ->
            BundledCablePart(WRParts.BUNDLED_CABLE, holder, side, 0u, BlockageUtils.UNBLOCKED, color, 0u, 0u, false)
        }
    }
}
