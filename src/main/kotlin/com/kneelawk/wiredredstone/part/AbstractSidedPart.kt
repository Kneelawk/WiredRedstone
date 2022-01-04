package com.kneelawk.wiredredstone.part

import alexiil.mc.lib.multipart.api.*
import alexiil.mc.lib.net.IMsgReadCtx
import alexiil.mc.lib.net.IMsgWriteCtx
import alexiil.mc.lib.net.NetByteBuf
import com.kneelawk.wiredredstone.util.SidedPos
import com.kneelawk.wiredredstone.util.getPos
import com.kneelawk.wiredredstone.util.getWorld
import com.kneelawk.wiredredstone.wirenet.NetNodeContainer
import com.kneelawk.wiredredstone.wirenet.SidedPartExtType
import com.kneelawk.wiredredstone.wirenet.getWireNetworkState
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Hand
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockView

/**
 * A part that is on the side of a block and can be part of the Redstone-ish network.
 *
 * Subtypes of this could be parts for wires, bundle cables, or gates.
 */
abstract class AbstractSidedPart(definition: PartDefinition, holder: MultipartHolder, val side: Direction) :
    AbstractPart(definition, holder), NetNodeContainer {
    companion object {
        fun getPart(world: BlockView, pos: SidedPos): AbstractSidedPart? {
            val container = MultipartUtil.get(world, pos.pos) ?: return null
            return container.getFirstPart(AbstractSidedPart::class.java)?.ctx?.getPart(pos.side)
        }
    }

    var ctx: SidedPartContext? = null
        private set

    constructor(definition: PartDefinition, holder: MultipartHolder, tag: NbtCompound) : this(
        definition, holder, Direction.byId(tag.getByte("side").toInt())
    )

    constructor(definition: PartDefinition, holder: MultipartHolder, buffer: NetByteBuf, ctx: IMsgReadCtx) : this(
        definition, holder, Direction.byId(buffer.readByte().toInt())
    )

    abstract override val partExtType: SidedPartExtType

    override fun toTag(): NbtCompound {
        val tag = super.toTag()
        tag.putByte("side", side.id.toByte())
        return tag
    }

    override fun writeCreationData(buffer: NetByteBuf, ctx: IMsgWriteCtx) {
        super.writeCreationData(buffer, ctx)
        buffer.writeByte(side.id)
    }

    override fun onAdded(bus: MultipartEventBus) {
        ctx = holder.container.getFirstPart(AbstractSidedPart::class.java)?.ctx ?: SidedPartContext(bus)
        ctx!!.setPart(side, this)
    }

    override fun onPlacedBy(player: PlayerEntity?, hand: Hand?) {
        val world = getWorld()
        if (!world.isClient && world is ServerWorld) {
            world.getWireNetworkState().controller.onChanged(world, getPos())
        }
    }

    override fun onRemoved() {
        ctx!!.setPart(side, null)

        val world = getWorld()
        if (!world.isClient && world is ServerWorld) {
            world.getWireNetworkState().controller.onChanged(world, getPos())
        }
    }

    fun getSidedPos(): SidedPos {
        return SidedPos(getPos().toImmutable(), side)
    }
}