package com.kneelawk.wiredredstone.item

import alexiil.mc.lib.multipart.api.MultipartContainer
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.item.ItemUsageContext

interface GateItem {
    @Environment(EnvType.CLIENT)
    fun getOfferForPlacementGhost(context: ItemUsageContext): MultipartContainer.PartOffer?
}