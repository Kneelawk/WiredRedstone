package com.kneelawk.wiredredstone

import com.kneelawk.wiredredstone.wirenet.PartExtType
import com.mojang.serialization.Lifecycle
import net.minecraft.util.registry.Registry
import net.minecraft.util.registry.RegistryKey
import net.minecraft.util.registry.SimpleRegistry

object WRRegistries {
    private val EXT_PART_TYPE_IDENTIFIER = WRConstants.id("ext_part_type")
    private val EXT_PART_TYPE_KEY: RegistryKey<Registry<PartExtType>> = RegistryKey.ofRegistry(EXT_PART_TYPE_IDENTIFIER)

    @JvmStatic
    val EXT_PART_TYPE = SimpleRegistry(EXT_PART_TYPE_KEY, Lifecycle.experimental(), null)

    @Suppress("unchecked_cast")
    fun init() {
        Registry.register(Registry.REGISTRIES as Registry<Registry<*>>, EXT_PART_TYPE_IDENTIFIER, EXT_PART_TYPE)
    }
}