package com.kneelawk.wiredredstone.client.render

import net.fabricmc.fabric.api.renderer.v1.RendererAccess
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial

object WRMaterials {
    val POWERED_MATERIAL: RenderMaterial by lazy {
        RendererAccess.INSTANCE.renderer!!.materialFinder().blendMode(0, BlendMode.CUTOUT).emissive(0, true)
            .disableAo(0, true).find()
    }

    val UNPOWERED_MATERIAL: RenderMaterial by lazy {
        RendererAccess.INSTANCE.renderer!!.materialFinder().blendMode(0, BlendMode.CUTOUT).find()
    }
}