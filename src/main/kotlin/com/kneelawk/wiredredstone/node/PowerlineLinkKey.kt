package com.kneelawk.wiredredstone.node

import com.kneelawk.graphlib.api.graph.LinkContext
import com.kneelawk.graphlib.api.graph.user.LinkKey
import net.minecraft.nbt.NbtElement
import net.minecraft.util.Identifier

object PowerlineLinkKey : LinkKey {
    override fun getTypeId(): Identifier = WRBlockNodes.POWERLINE_LINK

    override fun toTag(): NbtElement? = null

    override fun isAutomaticRemoval(ctx: LinkContext): Boolean = false
}
