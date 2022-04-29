package com.kneelawk.wiredredstone.client

import com.kneelawk.wiredredstone.client.render.*
import com.kneelawk.wiredredstone.client.render.part.WRPartRenderers

@Suppress("unused")
fun init() {
    WRMatrixFixer.init()
    WRShaders.init()
    WRSprites.init()
    WRModels.init()
    WRPartRenderers.init()
    WROutlineRenderer.init()
    WRGhostRenderer.init()
}
