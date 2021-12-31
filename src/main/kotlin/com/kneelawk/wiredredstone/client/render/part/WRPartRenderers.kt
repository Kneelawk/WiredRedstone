package com.kneelawk.wiredredstone.client.render.part

import alexiil.mc.lib.multipart.api.render.PartStaticModelRegisterEvent
import com.kneelawk.wiredredstone.part.key.RedAlloyWirePartKey

object WRPartRenderers {
    fun init() {
        PartStaticModelRegisterEvent.EVENT.register { event: PartStaticModelRegisterEvent.StaticModelRenderer ->
            event.register(RedAlloyWirePartKey::class.java, RedAlloyWirePartBaker)
        }
    }
}