package com.kneelawk.wiredredstone.client.render

import com.kneelawk.wiredredstone.client.render.part.WRPartRenderers
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin

object WRModels {
    fun init() {
        ModelLoadingPlugin.register { ctx ->
            for (baker in WRPartRenderers.bakers()) {
                baker.registerModels(ctx::addModels)
            }
        }
    }
}
