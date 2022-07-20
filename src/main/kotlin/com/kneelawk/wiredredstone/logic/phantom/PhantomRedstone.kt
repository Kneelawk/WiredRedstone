package com.kneelawk.wiredredstone.logic.phantom

import com.kneelawk.wiredredstone.WRConstants.id
import com.kneelawk.wiredredstone.WRLog
import com.kneelawk.wiredredstone.mixin.api.StorageHelper
import com.mojang.serialization.Lifecycle
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.registry.Registry
import net.minecraft.util.registry.RegistryKey
import net.minecraft.util.registry.SimpleRegistry
import net.minecraft.world.BlockView

object PhantomRedstone {
    private val REF_DECODER_ID = id("phantom_redstone_ref_decoder")
    private val REF_DECODER_KEY = RegistryKey.ofRegistry<PhantomRedstoneRefDecoder>(REF_DECODER_ID)

    val REF_DECODER_REGISTRY = SimpleRegistry(REF_DECODER_KEY, Lifecycle.experimental(), null)

    val SIDED_PART_ID = id("sided_part")

    @Suppress("unchecked")
    fun init() {
        Registry.register(Registry.REGISTRIES as Registry<Registry<*>>, REF_DECODER_ID, REF_DECODER_REGISTRY)

        Registry.register(REF_DECODER_REGISTRY, SIDED_PART_ID, SidedPartPhantomRedstoneRef.Decoder)

        ServerChunkEvents.CHUNK_LOAD.register { world, chunk ->
            try {
                StorageHelper.getPhantomRedstone(world).onWorldChunkLoad(chunk.pos)
            } catch (e: Exception) {
                WRLog.error("Error loading phantom redstone chunk. World: '{}'/{}", world, world.registryKey.value, e)
            }
        }
        ServerChunkEvents.CHUNK_UNLOAD.register { world, chunk ->
            try {
                val phantomRedstone = StorageHelper.getPhantomRedstone(world)
                phantomRedstone.saveChunk(chunk.pos)
                phantomRedstone.onWorldChunkUnload(chunk.pos)
            } catch (e: Exception) {
                WRLog.error("Error unloading phantom redstone chunk. World: '{}'/{}", world, world.registryKey.value, e)
            }
        }
        ServerTickEvents.END_WORLD_TICK.register { world ->
            try {
                StorageHelper.getPhantomRedstone(world).tick()
            } catch (e: Exception) {
                WRLog.error("Error ticking phantom redstone storage. World: '{}'/{}", world, world.registryKey.value, e)
            }
        }
        ServerWorldEvents.UNLOAD.register { _, world ->
            try {
                StorageHelper.getPhantomRedstone(world).close()
            } catch (e: Exception) {
                WRLog.error("Error closing phantom redstone storage. World: '{}'/{}", world, world.registryKey.value, e)
            }
        }
    }

    fun addRef(world: ServerWorld, pos: BlockPos, ref: PhantomRedstoneRef) {
        StorageHelper.getPhantomRedstone(world).add(pos, ref)
    }

    fun removeRef(world: ServerWorld, pos: BlockPos, ref: PhantomRedstoneRef) {
        StorageHelper.getPhantomRedstone(world).remove(pos, ref)
    }

    @JvmStatic
    fun getStrongRedstonePower(original: Int, world: BlockView, pos: BlockPos, oppositeFace: Direction): Int {
        if (original == 15) {
            return 15
        }

        if (world is ServerWorld) {
            val storage = StorageHelper.getPhantomRedstone(world)
            val set = storage.get(pos)

            if (set.isEmpty()) return 0

            val toRemove = mutableListOf<PhantomRedstoneRef>()

            var power = 0
            for (ref in set) {
                val lookup = ref.getStrongRedstonePower(original, world, pos, oppositeFace)
                if (!lookup.found) {
                    toRemove.add(ref)
                }
                if (lookup.power > power) {
                    power = lookup.power
                }
            }

            for (ref in toRemove) {
                storage.remove(pos, ref)
            }

            return power
        }

        return 0
    }

    @JvmStatic
    fun getWeakRedstonePower(original: Int, world: BlockView, pos: BlockPos, oppositeFace: Direction): Int {
        if (original == 15) {
            return 15
        }

        if (world is ServerWorld) {
            val storage = StorageHelper.getPhantomRedstone(world)
            val set = storage.get(pos)

            if (set.isEmpty()) return 0

            val toRemove = mutableListOf<PhantomRedstoneRef>()

            var power = 0
            for (ref in set) {
                val lookup = ref.getWeakRedstonePower(original, world, pos, oppositeFace)
                if (!lookup.found) {
                    toRemove.add(ref)
                }
                if (lookup.power > power) {
                    power = lookup.power
                }
            }

            for (ref in toRemove) {
                storage.remove(pos, ref)
            }

            return power
        }

        return 0
    }
}
