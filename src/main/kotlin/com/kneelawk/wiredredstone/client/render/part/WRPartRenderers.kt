package com.kneelawk.wiredredstone.client.render.part

import alexiil.mc.lib.multipart.api.render.PartModelKey
import alexiil.mc.lib.multipart.api.render.PartStaticModelRegisterEvent
import com.kneelawk.wiredredstone.part.key.BundledCablePartKey
import com.kneelawk.wiredredstone.part.key.GateDiodePartKey
import com.kneelawk.wiredredstone.part.key.InsulatedWirePartKey
import com.kneelawk.wiredredstone.part.key.RedAlloyWirePartKey
import kotlin.reflect.KClass

object WRPartRenderers {
    private val BAKERS = mutableListOf<PartBaker<*>>()

    @Suppress("UNCHECKED_CAST")
    fun init() {
        register(RedAlloyWirePartBaker, RedAlloyWirePartKey::class)
        register(InsulatedWirePartBaker, InsulatedWirePartKey::class)
        register(BundledCablePartBaker, BundledCablePartKey::class)
        register(GateDiodePartBaker, GateDiodePartKey::class)

        PartStaticModelRegisterEvent.EVENT.register { event: PartStaticModelRegisterEvent.StaticModelRenderer ->
            for (baker in BAKERS) {
                event.register(baker.clazz.java as Class<PartModelKey>, baker.baker as WRPartBaker<PartModelKey>)
            }
        }
    }

    fun <K : PartModelKey> register(baker: WRPartBaker<K>, clazz: KClass<K>) {
        BAKERS.add(PartBaker(baker, clazz))
    }

    fun bakers(): Sequence<WRPartBaker<*>> = BAKERS.asSequence().map { it.baker }

    private data class PartBaker<K : PartModelKey>(val baker: WRPartBaker<K>, val clazz: KClass<K>)
}