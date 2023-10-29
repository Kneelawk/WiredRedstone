package com.kneelawk.wiredredstone.part

import alexiil.mc.lib.multipart.api.PartDefinition
import com.kneelawk.wiredredstone.WRConstants

object WRParts {
    // Wires
    val RED_ALLOY_WIRE by lazy { definition("red_alloy_wire", ::RedAlloyWirePart, ::RedAlloyWirePart) }
    val INSULATED_WIRE by lazy { definition("insulated_wire", ::InsulatedWirePart, ::InsulatedWirePart) }
    val BUNDLED_CABLE by lazy { definition("bundled_cable", ::BundledCablePart, ::BundledCablePart) }

    val STANDING_RED_ALLOY_WIRE by lazy {
        definition("standing_red_alloy_wire", ::StandingRedAlloyWirePart, ::StandingRedAlloyWirePart)
    }
    val STANDING_INSULATED_WIRE by lazy {
        definition("standing_insulated_wire", ::StandingInsulatedWirePart, ::StandingInsulatedWirePart)
    }
    val STANDING_BUNDLED_CABLE by lazy { definition("standing_bundled_cable", ::StandingBundledCablePart, ::StandingBundledCablePart) }

    val POWERLINE_CONNECTOR by lazy {
        definition("powerline_connector", ::PowerlineConnectorPart, ::PowerlineConnectorPart)
    }

    // Gates
    val GATE_AND by lazy { definition("gate_and", ::GateAndPart, ::GateAndPart) }
    val GATE_DIODE by lazy { definition("gate_diode", ::GateDiodePart, ::GateDiodePart) }
    val GATE_NAND by lazy { definition("gate_nand", ::GateNandPart, ::GateNandPart) }
    val GATE_NOR by lazy { definition("gate_nor", ::GateNorPart, ::GateNorPart) }
    val GATE_NOT by lazy { definition("gate_not", ::GateNotPart, ::GateNotPart) }
    val GATE_OR by lazy { definition("gate_or", ::GateOrPart, ::GateOrPart) }
    val GATE_XNOR by lazy { definition("gate_xnor", ::GateXnorPart, ::GateXnorPart) }
    val GATE_XOR by lazy { definition("gate_xor", ::GateXorPart, ::GateXorPart) }
    val GATE_PROJECTOR_SIMPLE by lazy {
        definition("gate_projector_simple", ::GateProjectorSimplePart, ::GateProjectorSimplePart)
    }
    val GATE_REPEATER by lazy { definition("gate_repeater", ::GateRepeaterPart, ::GateRepeaterPart) }
    val GATE_RS_LATCH by lazy { definition("gate_rs_latch", ::GateRSLatchPart, ::GateRSLatchPart) }

    private fun definition(
        path: String, reader: PartDefinition.IPartNbtReader, loader: PartDefinition.IPartNetLoader
    ): PartDefinition {
        return PartDefinition(WRConstants.id(path), reader, loader)
    }

    fun init() {
        RED_ALLOY_WIRE.register()
        INSULATED_WIRE.register()
        BUNDLED_CABLE.register()

        STANDING_RED_ALLOY_WIRE.register()
        STANDING_INSULATED_WIRE.register()
        STANDING_BUNDLED_CABLE.register()

        POWERLINE_CONNECTOR.register()

        GATE_AND.register()
        GATE_DIODE.register()
        GATE_NAND.register()
        GATE_NOR.register()
        GATE_NOT.register()
        GATE_OR.register()
        GATE_XNOR.register()
        GATE_XOR.register()
        GATE_PROJECTOR_SIMPLE.register()
        GATE_REPEATER.register()
        GATE_RS_LATCH.register()
    }
}
