package com.kneelawk.wiredredstone.client.render.part

import alexiil.mc.lib.multipart.api.render.PartModelKey
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.math.MatrixStack

interface ProjectorPartBaker<K : PartModelKey> : WRPartBaker<K> {
    fun renderProjectorTarget(key: K, stack: MatrixStack, provider: VertexConsumerProvider)
}
