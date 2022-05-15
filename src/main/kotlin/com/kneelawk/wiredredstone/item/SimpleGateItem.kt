package com.kneelawk.wiredredstone.item

import alexiil.mc.lib.multipart.api.AbstractPart
import alexiil.mc.lib.multipart.api.MultipartContainer
import alexiil.mc.lib.multipart.api.MultipartHolder
import com.kneelawk.wiredredstone.util.PlacementUtils
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.block.Blocks
import net.minecraft.item.Item
import net.minecraft.item.ItemUsageContext
import net.minecraft.util.ActionResult
import net.minecraft.util.math.Direction

class SimpleGateItem(settings: Settings, private val creator: (MultipartHolder, Direction, Direction) -> AbstractPart) :
    Item(settings), GateItem {
    override fun useOnBlock(context: ItemUsageContext): ActionResult {
        val world = context.world
        if (world.isClient) {
            return ActionResult.PASS
        }

        val offer = PlacementUtils.tryPlaceGate(context, ::creator) ?: return ActionResult.FAIL

        PlacementUtils.finishPlacement(context, offer, Blocks.REPEATER.defaultState)

        return ActionResult.SUCCESS
    }

    @Environment(EnvType.CLIENT)
    override fun getOfferForPlacementGhost(context: ItemUsageContext): MultipartContainer.PartOffer? {
        return PlacementUtils.tryPlaceGate(context, ::creator)
    }

    private fun creator(side: Direction, direction: Direction): (MultipartHolder) -> AbstractPart {
        return { holder ->
            creator(holder, side, direction)
        }
    }
}