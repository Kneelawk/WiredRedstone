package com.kneelawk.wiredredstone.item

import com.kneelawk.wiredredstone.WRConstants
import com.kneelawk.wiredredstone.part.*
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.util.DyeColor
import net.minecraft.util.registry.Registry

object WRItems {
    private val WIRED_REDSTONE_ITEM_GROUP: ItemGroup by lazy {
        FabricItemGroupBuilder.build(WRConstants.id("wiredredstone")) { ItemStack(RED_ALLOY_WIRE) }
    }
    val WIRED_REDSTONE_ITEM_SETTINGS: Item.Settings by lazy { Item.Settings().group(WIRED_REDSTONE_ITEM_GROUP) }

    // Wires
    val RED_ALLOY_WIRE by lazy { RedAlloyWireItem(WIRED_REDSTONE_ITEM_SETTINGS) }

    val WHITE_INSULATED_WIRE by lazy { InsulatedWireItem(DyeColor.WHITE, WIRED_REDSTONE_ITEM_SETTINGS) }
    val ORANGE_INSULATED_WIRE by lazy { InsulatedWireItem(DyeColor.ORANGE, WIRED_REDSTONE_ITEM_SETTINGS) }
    val MAGENTA_INSULATED_WIRE by lazy { InsulatedWireItem(DyeColor.MAGENTA, WIRED_REDSTONE_ITEM_SETTINGS) }
    val LIGHT_BLUE_INSULATED_WIRE by lazy { InsulatedWireItem(DyeColor.LIGHT_BLUE, WIRED_REDSTONE_ITEM_SETTINGS) }
    val YELLOW_INSULATED_WIRE by lazy { InsulatedWireItem(DyeColor.YELLOW, WIRED_REDSTONE_ITEM_SETTINGS) }
    val LIME_INSULATED_WIRE by lazy { InsulatedWireItem(DyeColor.LIME, WIRED_REDSTONE_ITEM_SETTINGS) }
    val PINK_INSULATED_WIRE by lazy { InsulatedWireItem(DyeColor.PINK, WIRED_REDSTONE_ITEM_SETTINGS) }
    val GRAY_INSULATED_WIRE by lazy { InsulatedWireItem(DyeColor.GRAY, WIRED_REDSTONE_ITEM_SETTINGS) }
    val LIGHT_GRAY_INSULATED_WIRE by lazy { InsulatedWireItem(DyeColor.LIGHT_GRAY, WIRED_REDSTONE_ITEM_SETTINGS) }
    val CYAN_INSULATED_WIRE by lazy { InsulatedWireItem(DyeColor.CYAN, WIRED_REDSTONE_ITEM_SETTINGS) }
    val PURPLE_INSULATED_WIRE by lazy { InsulatedWireItem(DyeColor.PURPLE, WIRED_REDSTONE_ITEM_SETTINGS) }
    val BLUE_INSULATED_WIRE by lazy { InsulatedWireItem(DyeColor.BLUE, WIRED_REDSTONE_ITEM_SETTINGS) }
    val BROWN_INSULATED_WIRE by lazy { InsulatedWireItem(DyeColor.BROWN, WIRED_REDSTONE_ITEM_SETTINGS) }
    val GREEN_INSULATED_WIRE by lazy { InsulatedWireItem(DyeColor.GREEN, WIRED_REDSTONE_ITEM_SETTINGS) }
    val RED_INSULATED_WIRE by lazy { InsulatedWireItem(DyeColor.RED, WIRED_REDSTONE_ITEM_SETTINGS) }
    val BLACK_INSULATED_WIRE by lazy { InsulatedWireItem(DyeColor.BLACK, WIRED_REDSTONE_ITEM_SETTINGS) }

    val BUNDLED_CABLE by lazy { BundledCableItem(null, WIRED_REDSTONE_ITEM_SETTINGS) }
    val WHITE_BUNDLED_CABLE by lazy { BundledCableItem(DyeColor.WHITE, WIRED_REDSTONE_ITEM_SETTINGS) }
    val ORANGE_BUNDLED_CABLE by lazy { BundledCableItem(DyeColor.ORANGE, WIRED_REDSTONE_ITEM_SETTINGS) }
    val MAGENTA_BUNDLED_CABLE by lazy { BundledCableItem(DyeColor.MAGENTA, WIRED_REDSTONE_ITEM_SETTINGS) }
    val LIGHT_BLUE_BUNDLED_CABLE by lazy { BundledCableItem(DyeColor.LIGHT_BLUE, WIRED_REDSTONE_ITEM_SETTINGS) }
    val YELLOW_BUNDLED_CABLE by lazy { BundledCableItem(DyeColor.YELLOW, WIRED_REDSTONE_ITEM_SETTINGS) }
    val LIME_BUNDLED_CABLE by lazy { BundledCableItem(DyeColor.LIME, WIRED_REDSTONE_ITEM_SETTINGS) }
    val PINK_BUNDLED_CABLE by lazy { BundledCableItem(DyeColor.PINK, WIRED_REDSTONE_ITEM_SETTINGS) }
    val GRAY_BUNDLED_CABLE by lazy { BundledCableItem(DyeColor.GRAY, WIRED_REDSTONE_ITEM_SETTINGS) }
    val LIGHT_GRAY_BUNDLED_CABLE by lazy { BundledCableItem(DyeColor.LIGHT_GRAY, WIRED_REDSTONE_ITEM_SETTINGS) }
    val CYAN_BUNDLED_CABLE by lazy { BundledCableItem(DyeColor.CYAN, WIRED_REDSTONE_ITEM_SETTINGS) }
    val PURPLE_BUNDLED_CABLE by lazy { BundledCableItem(DyeColor.PURPLE, WIRED_REDSTONE_ITEM_SETTINGS) }
    val BLUE_BUNDLED_CABLE by lazy { BundledCableItem(DyeColor.BLUE, WIRED_REDSTONE_ITEM_SETTINGS) }
    val BROWN_BUNDLED_CABLE by lazy { BundledCableItem(DyeColor.BROWN, WIRED_REDSTONE_ITEM_SETTINGS) }
    val GREEN_BUNDLED_CABLE by lazy { BundledCableItem(DyeColor.GREEN, WIRED_REDSTONE_ITEM_SETTINGS) }
    val RED_BUNDLED_CABLE by lazy { BundledCableItem(DyeColor.RED, WIRED_REDSTONE_ITEM_SETTINGS) }
    val BLACK_BUNDLED_CABLE by lazy { BundledCableItem(DyeColor.BLACK, WIRED_REDSTONE_ITEM_SETTINGS) }

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

    // Crafting Materials
    val REDSTONE_ALLOY_INGOT by lazy { Item(WIRED_REDSTONE_ITEM_SETTINGS) }
    val STONE_PLATE by lazy { Item(WIRED_REDSTONE_ITEM_SETTINGS) }
    val REDSTONE_ANODE by lazy { Item(WIRED_REDSTONE_ITEM_SETTINGS) }
    val REDSTONE_CATHODE by lazy { Item(WIRED_REDSTONE_ITEM_SETTINGS) }
    val REDSTONE_INVERTING_CATHODE by lazy { Item(WIRED_REDSTONE_ITEM_SETTINGS) }
    val REDSTONE_DELAY_LINE by lazy { Item(WIRED_REDSTONE_ITEM_SETTINGS) }
    val REDSTONE_WIRE_PLATE by lazy { Item(WIRED_REDSTONE_ITEM_SETTINGS) }

    fun init() {
        register(RED_ALLOY_WIRE, "red_alloy_wire")

        register(WHITE_INSULATED_WIRE, "white_insulated_wire")
        register(ORANGE_INSULATED_WIRE, "orange_insulated_wire")
        register(MAGENTA_INSULATED_WIRE, "magenta_insulated_wire")
        register(LIGHT_BLUE_INSULATED_WIRE, "light_blue_insulated_wire")
        register(YELLOW_INSULATED_WIRE, "yellow_insulated_wire")
        register(LIME_INSULATED_WIRE, "lime_insulated_wire")
        register(PINK_INSULATED_WIRE, "pink_insulated_wire")
        register(GRAY_INSULATED_WIRE, "gray_insulated_wire")
        register(LIGHT_GRAY_INSULATED_WIRE, "light_gray_insulated_wire")
        register(CYAN_INSULATED_WIRE, "cyan_insulated_wire")
        register(PURPLE_INSULATED_WIRE, "purple_insulated_wire")
        register(BLUE_INSULATED_WIRE, "blue_insulated_wire")
        register(BROWN_INSULATED_WIRE, "brown_insulated_wire")
        register(GREEN_INSULATED_WIRE, "green_insulated_wire")
        register(RED_INSULATED_WIRE, "red_insulated_wire")
        register(BLACK_INSULATED_WIRE, "black_insulated_wire")

        register(BUNDLED_CABLE, "bundled_cable")
        register(WHITE_BUNDLED_CABLE, "white_bundled_cable")
        register(ORANGE_BUNDLED_CABLE, "orange_bundled_cable")
        register(MAGENTA_BUNDLED_CABLE, "magenta_bundled_cable")
        register(LIGHT_BLUE_BUNDLED_CABLE, "light_blue_bundled_cable")
        register(YELLOW_BUNDLED_CABLE, "yellow_bundled_cable")
        register(LIME_BUNDLED_CABLE, "lime_bundled_cable")
        register(PINK_BUNDLED_CABLE, "pink_bundled_cable")
        register(GRAY_BUNDLED_CABLE, "gray_bundled_cable")
        register(LIGHT_GRAY_BUNDLED_CABLE, "light_gray_bundled_cable")
        register(CYAN_BUNDLED_CABLE, "cyan_bundled_cable")
        register(PURPLE_BUNDLED_CABLE, "purple_bundled_cable")
        register(BLUE_BUNDLED_CABLE, "blue_bundled_cable")
        register(BROWN_BUNDLED_CABLE, "brown_bundled_cable")
        register(GREEN_BUNDLED_CABLE, "green_bundled_cable")
        register(RED_BUNDLED_CABLE, "red_bundled_cable")
        register(BLACK_BUNDLED_CABLE, "black_bundled_cable")

        register(GATE_DIODE, "gate_diode")
        register(GATE_AND, "gate_and")
        register(GATE_OR, "gate_or")
        register(GATE_NAND, "gate_nand")
        register(GATE_NOR, "gate_nor")
        register(GATE_NOT, "gate_not")
        register(GATE_REPEATER, "gate_repeater")
        register(GATE_RS_LATCH, "gate_rs_latch")
        register(GATE_PROJECTOR_SIMPLE, "gate_projector_simple")

        register(REDSTONE_ALLOY_INGOT, "redstone_alloy_ingot")
        register(STONE_PLATE, "stone_plate")
        register(REDSTONE_ANODE, "redstone_anode")
        register(REDSTONE_CATHODE, "redstone_cathode")
        register(REDSTONE_INVERTING_CATHODE, "redstone_inverting_cathode")
        register(REDSTONE_DELAY_LINE, "redstone_delay_line")
        register(REDSTONE_WIRE_PLATE, "redstone_wire_plate")
    }

    private fun register(item: Item, name: String) {
        Registry.register(Registry.ITEM, WRConstants.id(name), item)
    }
}
