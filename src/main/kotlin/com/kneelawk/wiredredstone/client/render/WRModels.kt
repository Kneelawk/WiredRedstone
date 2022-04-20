package com.kneelawk.wiredredstone.client.render

import com.kneelawk.wiredredstone.client.render.part.WRPartRenderers
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry

object WRModels {
    fun init() {
        ModelLoadingRegistry.INSTANCE.registerModelProvider { _, out ->
            for (baker in WRPartRenderers.bakers()) {
                baker.registerModels(out)
            }
        }
    }
}