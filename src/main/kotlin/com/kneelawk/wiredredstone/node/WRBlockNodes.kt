package com.kneelawk.wiredredstone.node

import com.kneelawk.graphlib.api.graph.GraphUniverse
import com.kneelawk.graphlib.api.graph.user.BlockNodeType
import com.kneelawk.graphlib.api.graph.user.LinkEntityType
import com.kneelawk.graphlib.api.graph.user.LinkKeyType
import com.kneelawk.graphlib.api.graph.user.SyncProfile
import com.kneelawk.graphlib.api.util.CacheCategory
import com.kneelawk.graphlib.api.world.SaveMode
import com.kneelawk.wiredredstone.WRConstants.id
import com.kneelawk.wiredredstone.config.CommonConfig
import java.util.function.Supplier

object WRBlockNodes {
    val RED_ALLOY_WIRE = BlockNodeType.of(id("red_alloy_wire"), RedAlloyWireBlockNode.Decoder)
    val INSULATED_WIRE = BlockNodeType.of(id("insulated_wire"), InsulatedWireBlockNode.Decoder)
    val BUNDLED_CABLE = BlockNodeType.of(id("bundled_cable"), BundledCableBlockNode.Decoder)

    val STANDING_RED_ALLOY_WIRE =
        BlockNodeType.of(id("standing_red_alloy_wire"), Supplier { StandingRedAlloyBlockNode })
    val STANDING_INSULATED_WIRE =
        BlockNodeType.of(id("standing_insulated_wire"), StandingInsulatedWireBlockNode.Decoder)
    val STANDING_BUNDLED_CABLE = BlockNodeType.of(id("standing_bundled_cable"), StandingBundledCableBlockNode.Decoder)

    val POWERLINE_CONNECTOR = BlockNodeType.of(
        id("powerline_connector"), PowerlineConnectorBlockNode.Decoder, PowerlineConnectorBlockNode.Decoder
    )
    val POWERLINE_LINK = LinkKeyType.of(id("powerline"), Supplier { PowerlineLinkKey })
    val POWERLINE_LINK_ENTITY =
        LinkEntityType.of(id("powerline"), PowerlineLinkEntity.Decoder, PowerlineLinkEntity.Decoder)

    val GATE_AND = BlockNodeType.of(id("gate_and"), GateAndBlockNode.Decoder)
    val GATE_DIODE = BlockNodeType.of(id("gate_diode"), GateDiodeBlockNode.Decoder)
    val GATE_NAND = BlockNodeType.of(id("gate_nand"), GateNandBlockNode.Decoder)
    val GATE_NOR = BlockNodeType.of(id("gate_nor"), GateNorBlockNode.Decoder)
    val GATE_NOT = BlockNodeType.of(id("gate_not"), GateNotBlockNode.Decoder)
    val GATE_OR = BlockNodeType.of(id("gate_or"), GateOrBlockNode.Decoder)
    val GATE_XNOR = BlockNodeType.of(id("gate_xnor"), GateXnorBlockNode.Decoder)
    val GATE_XOR = BlockNodeType.of(id("gate_xor"), GateXorBlockNode.Decoder)
    val GATE_PROJECTOR_SIMPLE = BlockNodeType.of(id("gate_projector_simple"), GateProjectorSimpleBlockNode.Decoder)
    val GATE_REPEATER = BlockNodeType.of(id("gate_repeater"), GateRepeaterBlockNode.Decoder)
    val GATE_RS_LATCH = BlockNodeType.of(id("gate_rs_latch"), GateRSLatchBlockNode.Decoder)

    val REDSTONE_CARRIERS = CacheCategory.of(RedstoneCarrierBlockNode::class.java)

    private val SYNC_PROFILE = SyncProfile.of(CacheCategory.of { it.node is PowerlineConnectorBlockNode })

    val WIRE_NET by lazy {
        GraphUniverse.builder()
            .saveMode(if (CommonConfig.local.incrementalGraphSaves) SaveMode.INCREMENTAL else SaveMode.UNLOAD)
            .synchronizeToClient(SYNC_PROFILE).build(id("wire_net"))
    }

    fun init() {
        WIRE_NET.addDiscoverer(WRBlockNodeDiscoverer)

        WIRE_NET.addNodeTypes(
            RED_ALLOY_WIRE,
            INSULATED_WIRE,
            BUNDLED_CABLE,

            STANDING_RED_ALLOY_WIRE,
            STANDING_INSULATED_WIRE,
            STANDING_BUNDLED_CABLE,

            POWERLINE_CONNECTOR,

            GATE_AND,
            GATE_DIODE,
            GATE_NAND,
            GATE_NOR,
            GATE_NOT,
            GATE_OR,
            GATE_XNOR,
            GATE_XOR,
            GATE_PROJECTOR_SIMPLE,
            GATE_REPEATER,
            GATE_RS_LATCH
        )

        WIRE_NET.addLinkKeyType(POWERLINE_LINK)
        WIRE_NET.addLinkEntityType(POWERLINE_LINK_ENTITY)
        WIRE_NET.addCacheCategory(REDSTONE_CARRIERS)

        WIRE_NET.register()
    }
}
