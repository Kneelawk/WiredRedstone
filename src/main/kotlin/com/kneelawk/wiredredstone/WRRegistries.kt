package com.kneelawk.wiredredstone

import com.kneelawk.wiredredstone.wirenet.PartExtType
import com.mojang.serialization.Lifecycle
import net.minecraft.util.registry.Registry
import net.minecraft.util.registry.RegistryKey
import net.minecraft.util.registry.SimpleRegistry

object WRRegistries {
    val EXT_PART_TYPE_IDENTIFIER = WRConstants.id("ext_part_type")

    val EXT_PART_TYPE_KEY: RegistryKey<Registry<PartExtType>> by lazy {
        RegistryKey.ofRegistry(EXT_PART_TYPE_IDENTIFIER)
    }

    @JvmStatic
    lateinit var EXT_PART_TYPE: Registry<PartExtType>

    @Suppress("unchecked_cast")
    fun init() {
        EXT_PART_TYPE = Registry.register(
            Registry.REGISTRIES as Registry<Registry<*>>, EXT_PART_TYPE_IDENTIFIER,
            SimpleRegistry(EXT_PART_TYPE_KEY, Lifecycle.experimental(), null)
        )
    }
}