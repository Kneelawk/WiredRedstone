package com.kneelawk.wiredredstone.client

import com.kneelawk.wiredredstone.client.render.WRModels
import com.kneelawk.wiredredstone.client.render.WRSprites
import com.kneelawk.wiredredstone.client.render.part.WRPartRenderers

@Suppress("unused")
fun init() {
    WRSprites.init()
    WRModels.init()
    WRPartRenderers.init()
}
