package com.kneelawk.wiredredstone.client

import com.kneelawk.wiredredstone.client.render.*
import com.kneelawk.wiredredstone.client.render.part.WRPartRenderers
import com.kneelawk.wiredredstone.client.screen.WRScreens
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment

@Suppress("unused")
@Environment(EnvType.CLIENT)
fun init() {
    WRMatrixFixer.init()
    WRShaders.init()
    WRSprites.init()
    WRModels.init()
    WRPartRenderers.init()
    WROutlineRenderer.init()
    WRGhostRenderer.init()
    WRScreens.init()
    WRTextRenderer.init()
    WRProjectionerRenderer.init()
}
