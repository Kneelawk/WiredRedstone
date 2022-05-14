package com.kneelawk.wiredredstone.node

import com.kneelawk.graphlib.GraphLib
import com.kneelawk.wiredredstone.WRConstants.id
import net.minecraft.util.registry.Registry

object WRBlockNodes {
    val RED_ALLOY_WIRE_ID = id("red_alloy_wire")
    val INSULATED_WIRE_ID = id("insulated_wire")
    val BUNDLED_CABLE_ID = id("bundled_cable")
    val GATE_DIODE_ID = id("gate_diode")

    fun init() {
        Registry.register(GraphLib.BLOCK_NODE_DECODER, RED_ALLOY_WIRE_ID, RedAlloyWireBlockNode.Decoder)
        Registry.register(GraphLib.BLOCK_NODE_DECODER, INSULATED_WIRE_ID, InsulatedWireBlockNode.Decoder)
        Registry.register(GraphLib.BLOCK_NODE_DECODER, BUNDLED_CABLE_ID, BundledCableBlockNode.Decoder)
        Registry.register(GraphLib.BLOCK_NODE_DECODER, GATE_DIODE_ID, GateDiodeBlockNode.Decoder)
    }
}