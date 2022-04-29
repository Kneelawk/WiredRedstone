package com.kneelawk.wiredredstone.client.render.part

import alexiil.mc.lib.multipart.api.render.PartModelKey
import alexiil.mc.lib.multipart.api.render.PartStaticModelRegisterEvent
import com.kneelawk.wiredredstone.WRConstants
import com.kneelawk.wiredredstone.part.key.BundledCablePartKey
import com.kneelawk.wiredredstone.part.key.GateDiodePartKey
import com.kneelawk.wiredredstone.part.key.InsulatedWirePartKey
import com.kneelawk.wiredredstone.part.key.RedAlloyWirePartKey
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener
import net.minecraft.resource.ResourceManager
import net.minecraft.resource.ResourceType
import net.minecraft.util.Identifier
import kotlin.reflect.KClass

object WRPartRenderers {
    private val BAKERS = mutableListOf<PartBaker<*>>()
    private val BAKER_MAP = mutableMapOf<KClass<out PartModelKey>, WRPartBaker<*>>()

    @Suppress("UNCHECKED_CAST")
    fun init() {
        register(RedAlloyWirePartBaker, RedAlloyWirePartKey::class)
        register(InsulatedWirePartBaker, InsulatedWirePartKey::class)
        register(BundledCablePartBaker, BundledCablePartKey::class)
        register(GateDiodePartBaker, GateDiodePartKey::class)

        PartStaticModelRegisterEvent.EVENT.register { event: PartStaticModelRegisterEvent.StaticModelRenderer ->
            for ((baker, clazz) in BAKERS) {
                event.register(clazz.java as Class<PartModelKey>, baker as WRPartBaker<PartModelKey>)
            }
        }

        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES)
            .registerReloadListener(object : SimpleSynchronousResourceReloadListener {
                override fun reload(manager: ResourceManager) {
                    for (baker in BAKERS) {
                        baker.baker.invalidateCaches()
                    }
                }

                override fun getFabricId(): Identifier = WRConstants.id("part_cache_invalidator")
            })
    }

    fun <K : PartModelKey> register(baker: WRPartBaker<K>, clazz: KClass<K>) {
        BAKERS.add(PartBaker(baker, clazz))
        BAKER_MAP[clazz] = baker
    }

    fun bakers(): Sequence<WRPartBaker<*>> = BAKERS.asSequence().map { it.baker }

    @Suppress("UNCHECKED_CAST")
    fun <K : PartModelKey> bakerFor(clazz: KClass<out K>): WRPartBaker<K> {
        return BAKER_MAP[clazz] as WRPartBaker<K>
    }

    private data class PartBaker<K : PartModelKey>(val baker: WRPartBaker<K>, val clazz: KClass<K>)
}