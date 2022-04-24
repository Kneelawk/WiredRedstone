package com.kneelawk.wiredredstone.partext

import com.kneelawk.wiredredstone.WRConstants.id
import com.kneelawk.wiredredstone.WRRegistries.EXT_PART_TYPE
import net.minecraft.util.registry.Registry

object WRPartExts {
    fun init() {
        Registry.register(EXT_PART_TYPE, id("red_alloy_wire"), RedAlloyWirePartExt.Type)
        Registry.register(EXT_PART_TYPE, id("insulated_wire"), InsulatedWirePartExt.Type)
        Registry.register(EXT_PART_TYPE, id("bundled_cable"), BundledCablePartExt.Type)
        Registry.register(EXT_PART_TYPE, id("gate_diode"), GateDiodePartExt.Type)
    }
}