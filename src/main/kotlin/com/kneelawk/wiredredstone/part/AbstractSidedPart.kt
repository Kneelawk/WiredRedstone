package com.kneelawk.wiredredstone.part

import alexiil.mc.lib.multipart.api.*
import alexiil.mc.lib.multipart.api.event.*
import alexiil.mc.lib.net.IMsgReadCtx
import alexiil.mc.lib.net.IMsgWriteCtx
import alexiil.mc.lib.net.NetByteBuf
import com.kneelawk.wiredredstone.node.WRBlockNodes.WIRE_NET
import com.kneelawk.wiredredstone.part.event.WRPartPreMoveEvent
import com.kneelawk.wiredredstone.util.*
import com.kneelawk.wiredredstone.util.connectable.ConnectableUtils
import net.minecraft.item.ItemStack
import net.minecraft.loot.context.LootContext
import net.minecraft.loot.context.LootContextParameters
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import net.minecraft.util.shape.VoxelShape

/**
 * A part that is on the side of a block and can be part of the Redstone-ish network.
 *
 * Subtypes of this could be parts for wires, bundle cables, or gates.
 */
abstract class AbstractSidedPart(definition: PartDefinition, holder: MultipartHolder, side: Direction) :
    AbstractPart(definition, holder), BlockNodeContainer, SidedPart {

    final override var side: Direction = side
        private set
    private var ctx: SidedPartContext? = null

    private val shapeCache = mutableMapOf<BlockPos, VoxelShape>()

    protected var initialized = false
    private var noBreak = false
    private var unloading = false

    constructor(definition: PartDefinition, holder: MultipartHolder, tag: NbtCompound) : this(
        definition, holder, Direction.byId(tag.getByte("side").toInt())
    ) {
        // defaults to false
        noBreak = tag.getBoolean("noBreak")
    }

    constructor(definition: PartDefinition, holder: MultipartHolder, buffer: NetByteBuf, ctx: IMsgReadCtx) : this(
        definition, holder, Direction.byId(buffer.readFixedBits(3))
    )

    override fun toTag(): NbtCompound {
        val tag = super.toTag()
        tag.putByte("side", side.id.toByte())

        if (noBreak) {
            tag.putBoolean("noBreak", true)
        }

        return tag
    }

    override fun writeCreationData(buffer: NetByteBuf, ctx: IMsgWriteCtx) {
        super.writeCreationData(buffer, ctx)
        buffer.writeFixedBits(side.id, 3)
    }

    override fun writeRenderData(buffer: NetByteBuf, ctx: IMsgWriteCtx) {
        super.writeRenderData(buffer, ctx)
        buffer.writeFixedBits(side.id, 3)
    }

    override fun readRenderData(buffer: NetByteBuf, ctx: IMsgReadCtx) {
        super.readRenderData(buffer, ctx)
        // No SideContext on the client-side
        side = Direction.byId(buffer.readFixedBits(3))
    }

    override fun getSidedContext(): SidedPartContext {
        return ctx.requireNonNull("SidedPartContext is still null (onAdded must not have been called yet)")
    }

    override fun onAdded(bus: MultipartEventBus) {
        // Ctx is used on both the client and the server
        ctx = holder.container.getFirstPart(AbstractSidedPart::class.java) { it.ctx != null }?.ctx
            ?: SidedPartContext(bus)
        ctx!!.setPart(side, this)

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
                        WIRE_NET.getGraphWorld(world).updateConnections(getSidedPos())
                    }
                }
            }
        }

        // Connection Blockage Detection

        bus.addListener(this, PartAddedEvent::class.java) {
            val world = getWorld()
            if (it.part !== this && world is ServerWorld) {
                // Something could be blocking our connection
                WIRE_NET.getGraphWorld(world).updateConnections(getSidedPos())
            }
        }

        bus.addListener(this, PartRemovedEvent::class.java) {
            val world = getWorld()
            if (it.removed !== this && world is ServerWorld) {
                // A connection could be unblocked
                WIRE_NET.getGraphWorld(world).updateConnections(getSidedPos())
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

        // Rotation Handling

        bus.addContextlessListener(this, PartPreTransformEvent::class.java) {
            // Remove self from the sided context until after the transformation so different parts don't trample each
            // other
            ctx.requireNonNull("Attempted rotation but ctx is null!")
                .setPart(side, null)
        }

        bus.addListener(this, PartTransformEvent::class.java) { e ->
            side = e.transformation.map(side)
        }

        bus.addContextlessListener(this, PartPostTransformEvent::class.java) {
            // Now re-add self since all the transformations are done
            ctx.requireNonNull("Attempted rotation but ctx is null!")
                .setPart(side, this)

            // Update the nodes here
            val world = getWorld()
            if (world is ServerWorld) {
                WIRE_NET.getGraphWorld(world).updateNodes(getPos())
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
                WIRE_NET.getGraphWorld(world).updateNodes(getPos())
            }
        }
    }

    protected open fun shouldBreak(): Boolean {
        val world = getWorld()
        val offset = getPos().offset(side)
        val state = world.getBlockState(offset)
        return !PlacementUtils.isValidFace(state, world, offset, side.opposite)
    }

    fun removeAndDrop() {
        val world = getWorld() as? ServerWorld ?: return
        val pos = getPos()
        val state = world.getBlockState(pos)
        val origin = Vec3d.of(pos).add(shape.boundingBox.center)

        playBreakSound()
        sendNetworkUpdate(this, NET_SPAWN_BREAK_PARTICLES)

        val context = LootContext.Builder(world).random(world.random)
            .parameter(LootContextParameters.BLOCK_STATE, state)
            .parameter(LootContextParameters.ORIGIN, origin)
            .parameter(LootContextParameters.TOOL, ItemStack.EMPTY)
            .parameter(PartLootParams.BROKEN_PART, PartLootParams.BrokenSinglePart(this))
            // No good way to tell if other parts are affected by this too
            .parameter(PartLootParams.ADDITIONAL_PARTS, emptyArray())
            .optionalParameter(LootContextParameters.BLOCK_ENTITY, holder.container.multipartBlockEntity)
            .build(PartLootParams.PART_TYPE)

        addDrops(SimpleItemDropTarget(world, origin), context)

        holder.remove()
    }

    override fun onRemoved() {
        // Ctx is used on both the client and the server
        ctx.requireNonNull("Tried to remove a part before it was added! (SidedPartContext is still null)")
            .setPart(side, null)

        // Nothing else here needs to be done on the client
        if (getWorld().isClient) return

        val world = getWorld()
        if (!world.isClient && world is ServerWorld) {
            WIRE_NET.getGraphWorld(world).updateNodes(getPos())
        }
    }

    override fun getPos(): BlockPos {
        return holder.container.multipartPos
    }
}
