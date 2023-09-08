package com.kneelawk.wiredredstone.item

import com.kneelawk.wiredredstone.WRConstants
import com.kneelawk.wiredredstone.part.*
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.DyeColor

object WRItems {
    internal val WIRED_REDSTONE_ITEMS = mutableListOf<ItemStack>()
    private val WIRED_REDSTONE_ITEM_GROUP: ItemGroup by lazy {
        FabricItemGroup.builder()
            .name(WRConstants.tt("itemGroup", "wiredredstone"))
            .icon { ItemStack(RED_ALLOY_WIRE) }
            .entries { _, entries -> entries.addStacks(WIRED_REDSTONE_ITEMS) }.build()
    }
    val WIRED_REDSTONE_ITEM_SETTINGS: Item.Settings by lazy { Item.Settings() }
    val WIRED_REDSTONE_TOOL_SETTINGS: Item.Settings by lazy { Item.Settings().maxCount(1) }

    // Wires
    val RED_ALLOY_WIRE by lazy { RedAlloyWireItem(WIRED_REDSTONE_ITEM_SETTINGS) }
    val INSULATED_WIRES by lazy {
        DyeColor.values().associateWith { InsulatedWireItem(it, WIRED_REDSTONE_ITEM_SETTINGS) }
    }
    val BUNDLED_CABLES by lazy {
        (listOf<DyeColor?>(null) + DyeColor.values()).associateWith {
            BundledCableItem(it, WIRED_REDSTONE_ITEM_SETTINGS)
        }
    }

    val STANDING_RED_ALLOY_WIRE by lazy { StandingRedAlloyWireItem(WIRED_REDSTONE_ITEM_SETTINGS) }
    val STANDING_INSULATED_WIRES by lazy {
        DyeColor.values().associateWith { StandingInsulatedWireItem(it, WIRED_REDSTONE_ITEM_SETTINGS) }
    }
    val STANDING_BUNDLED_CABLES by lazy {
        (listOf<DyeColor?>(null) + DyeColor.values()).associateWith {
            StandingBundledCableItem(it, WIRED_REDSTONE_ITEM_SETTINGS)
        }
    }

    val POWERLINE_CONNECTOR by lazy { PowerlineConnectorItem(WIRED_REDSTONE_ITEM_SETTINGS) }
    val POWERLINE_WIRE by lazy { PowerlineWireItem(WIRED_REDSTONE_ITEM_SETTINGS) }

    // Gates
    val GATE_DIODE by lazy {
        SimpleGateItem(WIRED_REDSTONE_ITEM_SETTINGS) { holder, side, direction ->
            GateDiodePart(WRParts.GATE_DIODE, holder, side, 0u, direction, 0, 0, 0)
        }
    }
    val GATE_AND by lazy {
        SimpleGateItem(WIRED_REDSTONE_ITEM_SETTINGS) { holder, side, direction ->
            GateAndPart(WRParts.GATE_AND, holder, side, 0u, direction, 0, 0, 0, 0, 0, true, true, true)
        }
    }
    val GATE_OR by lazy {
        SimpleGateItem(WIRED_REDSTONE_ITEM_SETTINGS) { holder, side, direction ->
            GateOrPart(WRParts.GATE_OR, holder, side, 0u, direction, 0, 0, 0, 0, 0, true, true, true)
        }
    }
    val GATE_NAND by lazy {
        SimpleGateItem(WIRED_REDSTONE_ITEM_SETTINGS) { holder, side, direction ->
            GateNandPart(WRParts.GATE_NAND, holder, side, 0u, direction, 0, 0, 0, 15, 0, true, true, true)
        }
    }
    val GATE_NOR by lazy {
        SimpleGateItem(WIRED_REDSTONE_ITEM_SETTINGS) { holder, side, direction ->
            GateNorPart(WRParts.GATE_NOR, holder, side, 0u, direction, 0, 0, 0, 15, 0, true, true, true)
        }
    }
    val GATE_NOT by lazy {
        SimpleGateItem(WIRED_REDSTONE_ITEM_SETTINGS) { holder, side, direction ->
            GateNotPart(WRParts.GATE_NOT, holder, side, 0u, direction, 0, 15, 0)
        }
    }
    val GATE_REPEATER by lazy {
        SimpleGateItem(WIRED_REDSTONE_ITEM_SETTINGS) { holder, side, direction ->
            GateRepeaterPart(WRParts.GATE_REPEATER, holder, side, 0u, direction, 0, 0, 0, 0, 0, false)
        }
    }
    val GATE_RS_LATCH by lazy {
        SimpleGateItem(WIRED_REDSTONE_ITEM_SETTINGS) { holder, side, direction ->
            GateRSLatchPart(
                WRParts.GATE_RS_LATCH, holder, side, 0u, direction, GateRSLatchPart.LatchState.RESET, true, 0, 0, 0, 0
            )
        }
    }
    val GATE_PROJECTOR_SIMPLE by lazy {
        SimpleGateItem(WIRED_REDSTONE_ITEM_SETTINGS) { holder, side, direction ->
            GateProjectorSimplePart(WRParts.GATE_PROJECTOR_SIMPLE, holder, side, 0u, direction, 0, 0)
        }
    }

    // Tools
    val PROJECTION_VIEWER by lazy { ProjectionViewerItem(WIRED_REDSTONE_TOOL_SETTINGS) }

    // Crafting Materials
    val REDSTONE_ALLOY_INGOT by lazy { Item(WIRED_REDSTONE_ITEM_SETTINGS) }
    val STONE_PLATE by lazy { Item(WIRED_REDSTONE_ITEM_SETTINGS) }
    val REDSTONE_ANODE by lazy { Item(WIRED_REDSTONE_ITEM_SETTINGS) }
    val REDSTONE_CATHODE by lazy { Item(WIRED_REDSTONE_ITEM_SETTINGS) }
    val REDSTONE_INVERTING_CATHODE by lazy { Item(WIRED_REDSTONE_ITEM_SETTINGS) }
    val REDSTONE_DELAY_LINE by lazy { Item(WIRED_REDSTONE_ITEM_SETTINGS) }
    val REDSTONE_WIRE_PLATE by lazy { Item(WIRED_REDSTONE_ITEM_SETTINGS) }
    val ENDER_REDSTONE_MIXTURE by lazy { Item(WIRED_REDSTONE_ITEM_SETTINGS) }
    val OBSIDIAN_STICK by lazy { Item(WIRED_REDSTONE_ITEM_SETTINGS) }
    val REDSTONE_PROJECTOR_TORCH by lazy { Item(WIRED_REDSTONE_ITEM_SETTINGS) }
    val REDSTONE_PROJECTOR_CATHODE by lazy { Item(WIRED_REDSTONE_ITEM_SETTINGS) }

    fun init() {
        register(RED_ALLOY_WIRE, "red_alloy_wire")
        for ((color, item) in INSULATED_WIRES) {
            register(item, color.getName() + "_insulated_wire")
        }
        for ((color, item) in BUNDLED_CABLES) {
            register(item, (color?.let { it.getName() + "_" } ?: "") + "bundled_cable")
        }

        register(STANDING_RED_ALLOY_WIRE, "standing_red_alloy_wire")
        for ((color, item) in STANDING_INSULATED_WIRES) {
            register(item, color.getName() + "_standing_insulated_wire")
        }
        for ((color, item) in STANDING_BUNDLED_CABLES) {
            register(item, (color?.let { it.getName() + "_" } ?: "") + "standing_bundled_cable")
        }

        register(POWERLINE_CONNECTOR, "powerline_connector", false)
        register(POWERLINE_WIRE, "powerline_wire", false)

        register(GATE_DIODE, "gate_diode")
        register(GATE_AND, "gate_and")
        register(GATE_OR, "gate_or")
        register(GATE_NAND, "gate_nand")
        register(GATE_NOR, "gate_nor")
        register(GATE_NOT, "gate_not")
        register(GATE_REPEATER, "gate_repeater")
        register(GATE_RS_LATCH, "gate_rs_latch")
        register(GATE_PROJECTOR_SIMPLE, "gate_projector_simple")

        register(PROJECTION_VIEWER, "projection_viewer")

        register(REDSTONE_ALLOY_INGOT, "redstone_alloy_ingot")
        register(STONE_PLATE, "stone_plate")
        register(REDSTONE_ANODE, "redstone_anode")
        register(REDSTONE_CATHODE, "redstone_cathode")
        register(REDSTONE_INVERTING_CATHODE, "redstone_inverting_cathode")
        register(REDSTONE_DELAY_LINE, "redstone_delay_line")
        register(REDSTONE_WIRE_PLATE, "redstone_wire_plate")
        register(ENDER_REDSTONE_MIXTURE, "ender_redstone_mixture")
        register(OBSIDIAN_STICK, "obsidian_stick")
        register(REDSTONE_PROJECTOR_TORCH, "redstone_projector_torch")
        register(REDSTONE_PROJECTOR_CATHODE, "redstone_projector_cathode")

        Registry.register(Registries.ITEM_GROUP, WRConstants.id("wiredredstone"), WIRED_REDSTONE_ITEM_GROUP)
    }

    private fun register(item: Item, name: String, creativeTab: Boolean = true) {
        Registry.register(Registries.ITEM, WRConstants.id(name), item)
        if (creativeTab) WIRED_REDSTONE_ITEMS.add(ItemStack(item))
    }
}
