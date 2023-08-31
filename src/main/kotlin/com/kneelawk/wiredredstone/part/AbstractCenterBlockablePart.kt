package com.kneelawk.wiredredstone.part

import alexiil.mc.lib.multipart.api.MultipartHolder
import alexiil.mc.lib.multipart.api.PartDefinition
import alexiil.mc.lib.net.IMsgReadCtx
import alexiil.mc.lib.net.NetByteBuf
import com.kneelawk.wiredredstone.util.bits.CenterConnectionUtils
import com.kneelawk.wiredredstone.util.getBlockEntity
import net.minecraft.nbt.NbtCompound

abstract class AbstractCenterBlockablePart : AbstractCenterConnectablePart, CenterBlockablePart {
    /**
     * Blockage cache. This is for helping in determining in which directions to emit a redstone signal.
     */
    var blockage: UByte
        private set

    constructor(definition: PartDefinition, holder: MultipartHolder, connections: UByte, blockage: UByte) : super(
        definition, holder, connections
    ) {
        this.blockage = blockage
    }

    constructor(definition: PartDefinition, holder: MultipartHolder, tag: NbtCompound) : super(
        definition, holder, tag
    ) {
        blockage = tag.getByte("blockage").toUByte()
    }

    constructor(definition: PartDefinition, holder: MultipartHolder, buffer: NetByteBuf, ctx: IMsgReadCtx) : super(
        definition, holder, buffer, ctx
    ) {
        // no need for blockage on client
        blockage = CenterConnectionUtils.NONE
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
