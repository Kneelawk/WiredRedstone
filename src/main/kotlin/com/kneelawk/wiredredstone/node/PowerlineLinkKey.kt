package com.kneelawk.wiredredstone.node

import com.kneelawk.graphlib.api.graph.LinkHolder
import com.kneelawk.graphlib.api.graph.user.LinkEntity
import com.kneelawk.graphlib.api.graph.user.LinkKey
import com.kneelawk.graphlib.api.graph.user.LinkKeyType
import net.minecraft.nbt.NbtElement

object PowerlineLinkKey : LinkKey {
    override fun getType(): LinkKeyType = WRBlockNodes.POWERLINE_LINK

    override fun toTag(): NbtElement? = null

    override fun isAutomaticRemoval(ctx: LinkHolder<LinkKey>): Boolean = false

    override fun shouldHaveLinkEntity(holder: LinkHolder<LinkKey>): Boolean = true

    override fun createLinkEntity(holder: LinkHolder<LinkKey>): LinkEntity = PowerlineLinkEntity()
}
