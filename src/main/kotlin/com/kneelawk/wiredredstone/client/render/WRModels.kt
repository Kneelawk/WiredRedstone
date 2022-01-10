package com.kneelawk.wiredredstone.client.render

import com.kneelawk.wiredredstone.WRConstants.id
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry

object WRModels {
    val GATE_DIODE_ON = id("block/gate_diode_on")
    val GATE_DIODE_OFF = id("block/gate_diode_off")

    fun init() {
        ModelLoadingRegistry.INSTANCE.registerModelProvider { _, out ->
            out.accept(GATE_DIODE_ON)
            out.accept(GATE_DIODE_OFF)
        }
    }
}