package com.kneelawk.wiredredstone.part

import alexiil.mc.lib.multipart.api.MultipartHolder
import alexiil.mc.lib.multipart.api.PartDefinition
import alexiil.mc.lib.multipart.api.render.PartModelKey
import alexiil.mc.lib.net.IMsgReadCtx
import alexiil.mc.lib.net.NetByteBuf
import com.kneelawk.graphlib.api.graph.user.BlockNode
import com.kneelawk.wiredredstone.node.StandingRedAlloyBlockNode
import com.kneelawk.wiredredstone.util.PixelBox
import com.kneelawk.wiredredstone.util.vs
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.shape.VoxelShape

class StandingRedAlloyWirePart : AbstractWRPart, CenterPart {
    companion object {
        val TEMP_SHAPE = PixelBox(7, 7, 7, 9, 9, 9).vs()
    }

    constructor(definition: PartDefinition, holder: MultipartHolder) : super(definition, holder)
    constructor(definition: PartDefinition, holder: MultipartHolder, tag: NbtCompound) : super(definition, holder, tag)
    constructor(definition: PartDefinition, holder: MultipartHolder, buffer: NetByteBuf, ctx: IMsgReadCtx) : super(definition, holder)

    override fun getShape(): VoxelShape {
        return TEMP_SHAPE
    }

    override fun getModelKey(): PartModelKey? {
        return null
    }

    override fun createBlockNodes(): Collection<BlockNode> {
        return listOf(StandingRedAlloyBlockNode)
    }
}
