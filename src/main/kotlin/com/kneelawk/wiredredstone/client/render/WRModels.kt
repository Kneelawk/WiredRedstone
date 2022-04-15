package com.kneelawk.wiredredstone.client.render

import com.kneelawk.wiredredstone.WRConstants.id
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry

object WRModels {
    val GATE_DIODE_BACKGROUND = id("block/gate_diode/background")
    val GATE_DIODE_ON = id("block/gate_diode/redstone_on")
    val GATE_DIODE_OFF = id("block/gate_diode/redstone_off")

    fun init() {
        ModelLoadingRegistry.INSTANCE.registerModelProvider { _, out ->
            out.accept(GATE_DIODE_BACKGROUND)
            out.accept(GATE_DIODE_ON)
            out.accept(GATE_DIODE_OFF)
        }
    }
}