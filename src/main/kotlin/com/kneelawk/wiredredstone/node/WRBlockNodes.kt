package com.kneelawk.wiredredstone.node

import com.kneelawk.graphlib.GraphLib
import com.kneelawk.wiredredstone.WRConstants.id
import net.minecraft.util.registry.Registry

object WRBlockNodes {
    val RED_ALLOY_WIRE_ID = id("red_alloy_wire")
    val INSULATED_WIRE_ID = id("insulated_wire")
    val BUNDLED_CABLE_ID = id("bundled_cable")
    val GATE_AND_ID = id("gate_and")
    val GATE_DIODE_ID = id("gate_diode")
    val GATE_NAND_ID = id("gate_nand")
    val GATE_NOR_ID = id("gate_nor")
    val GATE_NOT_ID = id("gate_not")
    val GATE_OR_ID = id("gate_or")
    val GATE_REPEATER_ID = id("gate_repeater")

    fun init() {
        Registry.register(GraphLib.BLOCK_NODE_DECODER, RED_ALLOY_WIRE_ID, RedAlloyWireBlockNode.Decoder)
        Registry.register(GraphLib.BLOCK_NODE_DECODER, INSULATED_WIRE_ID, InsulatedWireBlockNode.Decoder)
        Registry.register(GraphLib.BLOCK_NODE_DECODER, BUNDLED_CABLE_ID, BundledCableBlockNode.Decoder)
        Registry.register(GraphLib.BLOCK_NODE_DECODER, GATE_AND_ID, GateAndBlockNode.Decoder)
        Registry.register(GraphLib.BLOCK_NODE_DECODER, GATE_DIODE_ID, GateDiodeBlockNode.Decoder)
        Registry.register(GraphLib.BLOCK_NODE_DECODER, GATE_NAND_ID, GateNandBlockNode.Decoder)
        Registry.register(GraphLib.BLOCK_NODE_DECODER, GATE_NOR_ID, GateNorBlockNode.Decoder)
        Registry.register(GraphLib.BLOCK_NODE_DECODER, GATE_NOT_ID, GateNotBlockNode.Decoder)
        Registry.register(GraphLib.BLOCK_NODE_DECODER, GATE_OR_ID, GateOrBlockNode.Decoder)
        Registry.register(GraphLib.BLOCK_NODE_DECODER, GATE_REPEATER_ID, GateRepeaterBlockNode.Decoder)
    }
}
