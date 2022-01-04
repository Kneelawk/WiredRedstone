package com.kneelawk.wiredredstone.item

import alexiil.mc.lib.multipart.api.MultipartHolder
import alexiil.mc.lib.multipart.api.MultipartUtil
import com.kneelawk.wiredredstone.part.RedAlloyWirePart
import com.kneelawk.wiredredstone.part.WRParts
import net.minecraft.item.Item
import net.minecraft.item.ItemUsageContext
import net.minecraft.util.ActionResult

class RedAlloyWireItem(settings: Settings) : Item(settings) {
    override fun useOnBlock(context: ItemUsageContext): ActionResult {
        val world = context.world
        if (world.isClient) {
            return ActionResult.PASS
        }

        val creator = { holder: MultipartHolder ->
            RedAlloyWirePart(WRParts.RED_ALLOY_WIRE, holder, context.side.opposite, 0u, false)
        }

        // TODO: Fix non-solid placement issue
        val offer = MultipartUtil.offerNewPart(world, context.blockPos, creator)
            ?: MultipartUtil.offerNewPart(world, context.blockPos.offset(context.side), creator)
            ?: return ActionResult.FAIL

        offer.apply()
        offer.holder.part.onPlacedBy(context.player, context.hand)
        context.stack.decrement(1)

        return ActionResult.SUCCESS
    }
}