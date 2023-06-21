package com.kneelawk.wiredredstone.node

import alexiil.mc.lib.net.IMsgReadCtx
import alexiil.mc.lib.net.NetByteBuf
import com.kneelawk.graphlib.api.graph.user.*
import net.minecraft.nbt.NbtElement

class PowerlineLinkEntity : AbstractLinkEntity() {
    override fun getType(): LinkEntityType = WRBlockNodes.POWERLINE_LINK_ENTITY

    override fun toTag(): NbtElement? = null

    object Decoder : LinkEntityDecoder, LinkEntityPacketDecoder {
        override fun decode(tag: NbtElement?): LinkEntity = PowerlineLinkEntity()

        override fun decode(buf: NetByteBuf, msgCtx: IMsgReadCtx): LinkEntity = PowerlineLinkEntity()
    }
}
