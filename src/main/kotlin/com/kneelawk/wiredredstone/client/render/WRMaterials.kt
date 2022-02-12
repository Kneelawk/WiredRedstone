package com.kneelawk.wiredredstone.client.render

import io.vram.frex.api.material.MaterialConstants
import io.vram.frex.api.material.RenderMaterial
import io.vram.frex.api.renderer.Renderer

object WRMaterials {
    val POWERED_MATERIAL: RenderMaterial by lazy {
        Renderer.get().materials().materialFinder().preset(MaterialConstants.PRESET_CUTOUT).emissive(true)
            .disableAo(true).find()
    }

    val UNPOWERED_MATERIAL: RenderMaterial by lazy {
        Renderer.get().materials().materialFinder().preset(MaterialConstants.PRESET_CUTOUT).find()
    }
}