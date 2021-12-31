package com.kneelawk.wiredredstone.part

import alexiil.mc.lib.multipart.api.PartDefinition
import com.kneelawk.wiredredstone.WRConstants

object WRParts {
    val RED_ALLOY_WIRE by lazy { definition("red_alloy_wire", ::RedAlloyWirePart, ::RedAlloyWirePart) }

    private fun definition(path: String, reader: PartDefinition.IPartNbtReader, loader: PartDefinition.IPartNetLoader): PartDefinition {
        return PartDefinition(WRConstants.id(path), reader, loader)
    }

    fun init() {
        PartDefinition.PARTS[RED_ALLOY_WIRE.identifier] = RED_ALLOY_WIRE
    }
}