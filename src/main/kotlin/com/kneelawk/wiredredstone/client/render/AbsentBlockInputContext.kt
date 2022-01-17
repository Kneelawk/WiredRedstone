package com.kneelawk.wiredredstone.client.render

import com.kneelawk.wiredredstone.util.requireNonNull
import io.vram.frex.api.material.MaterialConstants
import io.vram.frex.api.math.MatrixStack
import io.vram.frex.api.model.BakedInputContext
import io.vram.frex.api.model.BlockModel
import io.vram.frex.api.model.InputContext
import net.minecraft.block.BlockState
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.model.BakedModel
import net.minecraft.util.math.BlockPos
import net.minecraft.world.BlockRenderView
import java.util.*

object AbsentBlockInputContext : BlockModel.BlockInputContext {
    private val random = Random(42)
    private val matrixStack = MatrixStack.create()

    override fun random(): Random {
        return random
    }

    override fun type(): InputContext.Type {
        return InputContext.Type.BLOCK
    }

    override fun overlay(): Int {
        return OverlayTexture.DEFAULT_UV
    }

    override fun matrixStack(): MatrixStack {
        return matrixStack
    }

    override fun bakedModel(): BakedModel? {
        return null
    }

    override fun cullTest(faceId: Int): Boolean {
        return true
    }

    override fun indexedColor(colorIndex: Int): Int {
        return -1
    }

    override fun defaultRenderType(): RenderLayer {
        return RenderLayer.getSolid()
    }

    override fun defaultPreset(): Int {
        return MaterialConstants.PRESET_SOLID
    }

    override fun blockState(): BlockState? {
        return null
    }

    override fun pos(): BlockPos? {
        return null
    }

    override fun blockView(): BlockRenderView {
        return MinecraftClient.getInstance().world.requireNonNull("Attempted to render a block but without any client world")
    }

    override fun isFluidModel(): Boolean {
        return false
    }

    override fun blockEntityRenderData(pos: BlockPos): Any? {
        return null
    }
}