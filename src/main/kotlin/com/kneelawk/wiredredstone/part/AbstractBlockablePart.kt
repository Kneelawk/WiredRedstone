package com.kneelawk.wiredredstone.part

import alexiil.mc.lib.multipart.api.MultipartHolder
import alexiil.mc.lib.multipart.api.PartDefinition
import alexiil.mc.lib.net.IMsgReadCtx
import alexiil.mc.lib.net.NetByteBuf
import com.kneelawk.wiredredstone.util.bits.BlockageUtils
import com.kneelawk.wiredredstone.util.getBlockEntity
import com.kneelawk.wiredredstone.util.maybeGetByte
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.Direction

abstract class AbstractBlockablePart : AbstractConnectablePart, BlockablePart {
    /**
     * Blockage cache. This is for helping in determining in which directions to emit a redstone signal.
     */
    var blockage: UByte
        private set

    constructor(
        definition: PartDefinition, holder: MultipartHolder, side: Direction, connections: UByte, blockage: UByte
    ) : super(
        definition, holder, side, connections
    ) {
        this.blockage = blockage
    }

    constructor(definition: PartDefinition, holder: MultipartHolder, tag: NbtCompound) : super(
        definition, holder, tag
    ) {
        blockage = tag.maybeGetByte("blockage")?.toUByte() ?: BlockageUtils.UNBLOCKED
    }

    constructor(definition: PartDefinition, holder: MultipartHolder, buffer: NetByteBuf, ctx: IMsgReadCtx) : super(
        definition, holder, buffer, ctx
    ) {
        // There is currently no real use for blockage on the client
        blockage = BlockageUtils.UNBLOCKED
    }

    override fun toTag(): NbtCompound {
        val tag = super.toTag()
        tag.putByte("blockage", blockage.toByte())
        return tag
    }

    override fun updateBlockage(blockage: UByte) {
        this.blockage = blockage
        getBlockEntity().markDirty()
    }
}
