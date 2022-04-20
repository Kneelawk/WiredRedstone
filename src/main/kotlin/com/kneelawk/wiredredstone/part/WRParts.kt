package com.kneelawk.wiredredstone.part

import alexiil.mc.lib.multipart.api.PartDefinition
import com.kneelawk.wiredredstone.WRConstants

object WRParts {
    // Wires
    val RED_ALLOY_WIRE by lazy { definition("red_alloy_wire", ::RedAlloyWirePart, ::RedAlloyWirePart) }
    val INSULATED_WIRE by lazy { definition("insulated_wire", ::InsulatedWirePart, ::InsulatedWirePart) }

    // Gates
    val GATE_DIODE by lazy { definition("gate_diode", ::GateDiodePart, ::GateDiodePart) }

    private fun definition(
        path: String, reader: PartDefinition.IPartNbtReader, loader: PartDefinition.IPartNetLoader
    ): PartDefinition {
        return PartDefinition(WRConstants.id(path), reader, loader)
    }

    fun init() {
        PartDefinition.PARTS[RED_ALLOY_WIRE.identifier] = RED_ALLOY_WIRE
        PartDefinition.PARTS[INSULATED_WIRE.identifier] = INSULATED_WIRE
        PartDefinition.PARTS[GATE_DIODE.identifier] = GATE_DIODE
    }
}