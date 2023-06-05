package com.kneelawk.wiredredstone.node

import com.kneelawk.graphlib.api.graph.GraphUniverse
import com.kneelawk.graphlib.api.graph.user.LinkKeyDecoder
import com.kneelawk.graphlib.api.util.CacheCategory
import com.kneelawk.graphlib.api.world.SaveMode
import com.kneelawk.wiredredstone.WRConstants.id

object WRBlockNodes {
    val RED_ALLOY_WIRE_ID = id("red_alloy_wire")
    val INSULATED_WIRE_ID = id("insulated_wire")
    val BUNDLED_CABLE_ID = id("bundled_cable")

    val POWERLINE_CONNECTOR = id("powerline_connector")
    val POWERLINE_LINK = id("powerline")

    val GATE_AND_ID = id("gate_and")
    val GATE_DIODE_ID = id("gate_diode")
    val GATE_NAND_ID = id("gate_nand")
    val GATE_NOR_ID = id("gate_nor")
    val GATE_NOT_ID = id("gate_not")
    val GATE_OR_ID = id("gate_or")
    val GATE_PROJECTOR_SIMPLE_ID = id("gate_projector_simple")
    val GATE_REPEATER_ID = id("gate_repeater")
    val GATE_RS_LATCH = id("gate_rs_latch")

    val REDSTONE_CARRIERS = CacheCategory.of(RedstoneCarrierBlockNode::class.java)

    val WIRE_NET by lazy {
        GraphUniverse.builder().saveMode(SaveMode.INCREMENTAL).build(id("wire_net"))
    }

    fun init() {
        WIRE_NET.addDiscoverer(WRBlockNodeDiscoverer)

        WIRE_NET.addNodeDecoders(
            mapOf(
                RED_ALLOY_WIRE_ID to RedAlloyWireBlockNode.Decoder,
                INSULATED_WIRE_ID to InsulatedWireBlockNode.Decoder,
                BUNDLED_CABLE_ID to BundledCableBlockNode.Decoder,

                POWERLINE_CONNECTOR to PowerlineConnectorBlockNode.Decoder,

                GATE_AND_ID to GateAndBlockNode.Decoder,
                GATE_DIODE_ID to GateDiodeBlockNode.Decoder,
                GATE_NAND_ID to GateNandBlockNode.Decoder,
                GATE_NOR_ID to GateNorBlockNode.Decoder,
                GATE_NOT_ID to GateNotBlockNode.Decoder,
                GATE_OR_ID to GateOrBlockNode.Decoder,
                GATE_PROJECTOR_SIMPLE_ID to GateProjectorSimpleBlockNode.Decoder,
                GATE_REPEATER_ID to GateRepeaterBlockNode.Decoder,
                GATE_RS_LATCH to GateRSLatchBlockNode.Decoder
            )
        )

        WIRE_NET.addLinkKeyDecoders(mapOf(POWERLINE_LINK to LinkKeyDecoder { PowerlineLinkKey }))
        WIRE_NET.addCacheCategories(REDSTONE_CARRIERS)

        WIRE_NET.register()
    }
}
