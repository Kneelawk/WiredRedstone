package com.kneelawk.wiredredstone.part

import alexiil.mc.lib.multipart.api.*
import alexiil.mc.lib.multipart.api.event.*
import com.kneelawk.wiredredstone.node.WRBlockNodes.WIRE_NET
import com.kneelawk.wiredredstone.part.event.WRPartPreMoveEvent
import com.kneelawk.wiredredstone.util.SimpleItemDropTarget
import com.kneelawk.wiredredstone.util.connectable.ConnectableUtils
import com.kneelawk.wiredredstone.util.getWorld
import net.minecraft.item.ItemStack
import net.minecraft.loot.context.LootContextParameterSet
import net.minecraft.loot.context.LootContextParameters
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.util.shape.VoxelShape

abstract class AbstractWRPart(definition: PartDefinition, holder: MultipartHolder) : AbstractPart(definition, holder),
    BlockNodeContainer, WRPart {
    private val shapeCache = mutableMapOf<BlockPos, VoxelShape>()

    protected var initialized = false
    private var noBreak = false
    private var unloading = false

    constructor(definition: PartDefinition, holder: MultipartHolder, tag: NbtCompound) : this(definition, holder) {
        // defaults to false
        noBreak = tag.getBoolean("noBreak")
    }

    override fun toTag(): NbtCompound {
        val tag = super.toTag()

        if (noBreak) {
            tag.putBoolean("noBreak", true)
        }

        return tag
    }

    override fun onAdded(bus: MultipartEventBus) {
        super.onAdded(bus)

        // nothing else here needs to be done on the client
        if (getWorld().isClient) return

        // Detects when a block supporting this one is broken

        bus.addRunOnceListener(this, PartTickEvent::class.java) {
            onFirstTick()
        }

        bus.addListener(this, WRPartPreMoveEvent::class.java) {
            it.setMovementNecessary()
            noBreak = true
        }

        // also handles some connection blockage detection
        bus.addListener(this, NeighbourUpdateEvent::class.java) {
            val world = getWorld()
            if (world is ServerWorld) {
                if (!noBreak && shouldBreak()) {
                    removeAndDrop()
                } else {
                    // updating connections is expensive, so we want to make sure we *really* need to do it first
                    // wait until our first tick to tell if we should update our connections
                    if (ConnectableUtils.shouldUpdateConnectionsForNeighborUpdate(
                            shapeCache, world, getPos(), it.pos
                        ) && initialized
                    ) {
                        // Something could be blocking our connection
                        updateConnections(world)
                    }
                }
            }
        }

        // Connection Blockage Detection

        bus.addListener(this, PartAddedEvent::class.java) {
            val world = getWorld()
            if (it.part !== this && world is ServerWorld) {
                // Something could be blocking our connection
                updateConnections(world)
            }
        }

        bus.addListener(this, PartRemovedEvent::class.java) {
            val world = getWorld()
            if (it.removed !== this && world is ServerWorld) {
                // A connection could be unblocked
                updateConnections(world)
            }
        }

        // Removal Detection

        bus.addContextlessListener(this, PartContainerState.ChunkUnload::class.java) {
            unloading = true
        }

        bus.addContextlessListener(this, PartContainerState.Invalidate::class.java) {
            if (!unloading) {
                onRemoved()
            }
        }
    }

    protected open fun onFirstTick() {
        val world = getWorld()
        if (world is ServerWorld) {
            noBreak = false
            initialized = true
            if (shouldBreak()) {
                removeAndDrop()
            } else {
                WIRE_NET.getServerGraphWorld(world).updateNodes(getPos())
            }
        }
    }

    protected open fun shouldBreak(): Boolean {
        return false
    }

    fun removeAndDrop() {
        val world = getWorld() as? ServerWorld ?: return
        val pos = getPos()
        val state = world.getBlockState(pos)
        val origin = Vec3d.of(pos).add(shape.boundingBox.center)

        playBreakSound()
        sendNetworkUpdate(this, NET_SPAWN_BREAK_PARTICLES)

        val params = LootContextParameterSet.Builder(world)
            .add(LootContextParameters.BLOCK_STATE, state)
            .add(LootContextParameters.ORIGIN, origin)
            .add(LootContextParameters.TOOL, ItemStack.EMPTY)
            .add(PartLootParams.BROKEN_PART, PartLootParams.BrokenSinglePart(this))
            // No good way to tell if other parts are affected by this too
            .add(PartLootParams.ADDITIONAL_PARTS, emptyArray())
            .addOptional(LootContextParameters.BLOCK_ENTITY, holder.container.multipartBlockEntity)
            .build(PartLootParams.PART_TYPE)

        addDrops(SimpleItemDropTarget(world, origin), params)

        holder.remove()
    }

    protected open fun updateConnections(world: ServerWorld) {
        WIRE_NET.getServerGraphWorld(world).updateConnections(getPos())
    }

    override fun onRemoved() {
        // Nothing else here needs to be done on the client
        if (getWorld().isClient) return

        val world = getWorld()
        if (!world.isClient && world is ServerWorld) {
            WIRE_NET.getServerGraphWorld(world).updateNodes(getPos())
        }
    }

    override fun getPos(): BlockPos {
        return holder.container.multipartPos
    }
}
