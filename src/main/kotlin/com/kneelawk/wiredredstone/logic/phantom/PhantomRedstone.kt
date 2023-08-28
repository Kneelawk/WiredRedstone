package com.kneelawk.wiredredstone.logic.phantom

import com.kneelawk.wiredredstone.WRConstants.id
import com.mojang.serialization.Lifecycle
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.SimpleRegistry
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockView

object PhantomRedstone {
    private val REF_DECODER_ID = id("phantom_redstone_ref_decoder")
    private val REF_DECODER_KEY = RegistryKey.ofRegistry<PhantomRedstoneRefDecoder>(REF_DECODER_ID)

    val REF_DECODER_REGISTRY = SimpleRegistry(REF_DECODER_KEY, Lifecycle.stable())

    val SIDED_PART_ID = id("sided_part")

    @Suppress("unchecked")
    fun init() {
        Registry.register(Registries.REGISTRY as Registry<Registry<*>>, REF_DECODER_ID, REF_DECODER_REGISTRY)

        Registry.register(REF_DECODER_REGISTRY, SIDED_PART_ID, SidedPartPhantomRedstoneRef.Decoder)
    }

    fun addRef(world: ServerWorld, pos: BlockPos, ref: PhantomRedstoneRef) {
        PhantomRedstoneStorage.get(world).add(pos, ref)
    }

    fun removeRef(world: ServerWorld, pos: BlockPos, ref: PhantomRedstoneRef) {
        PhantomRedstoneStorage.get(world).remove(pos, ref)
    }

    @JvmStatic
    fun getStrongRedstonePower(original: Int, world: BlockView, pos: BlockPos, oppositeFace: Direction): Int {
        if (original == 15) {
            return 15
        }

        if (world is ServerWorld) {
            val storage = PhantomRedstoneStorage.get(world)
            val holder = storage.get(pos) ?: return 0

            when (holder) {
                is PhantomRedstoneStorage.Single -> {
                    val lookup = holder.ref.getStrongRedstonePower(original, world, pos, oppositeFace)
                    if (!lookup.found) storage.remove(pos, holder.ref)
                    return lookup.power
                }
                is PhantomRedstoneStorage.Many -> {
                    val toRemove = mutableListOf<PhantomRedstoneRef>()

                    var power = 0
                    for (ref in holder.refs) {
                        val lookup = ref.getStrongRedstonePower(original, world, pos, oppositeFace)
                        if (!lookup.found) toRemove.add(ref)
                        if (lookup.power > power) power = lookup.power
                    }

                    for (ref in toRemove) {
                        storage.remove(pos, ref)
                    }

                    return power
                }
            }
        }

        return 0
    }

    @JvmStatic
    fun getWeakRedstonePower(original: Int, world: BlockView, pos: BlockPos, oppositeFace: Direction): Int {
        if (original == 15) {
            return 15
        }

        if (world is ServerWorld) {
            val storage = PhantomRedstoneStorage.get(world)
            val holder = storage.get(pos) ?: return 0

            when (holder) {
                is PhantomRedstoneStorage.Single -> {
                    val lookup = holder.ref.getWeakRedstonePower(original, world, pos, oppositeFace)
                    if (!lookup.found) storage.remove(pos, holder.ref)
                    return lookup.power
                }
                is PhantomRedstoneStorage.Many -> {
                    val toRemove = mutableListOf<PhantomRedstoneRef>()

                    var power = 0
                    for (ref in holder.refs) {
                        val lookup = ref.getWeakRedstonePower(original, world, pos, oppositeFace)
                        if (!lookup.found) toRemove.add(ref)
                        if (lookup.power > power) power = lookup.power
                    }

                    for (ref in toRemove) {
                        storage.remove(pos, ref)
                    }

                    return power
                }
            }
        }

        return 0
    }
}
