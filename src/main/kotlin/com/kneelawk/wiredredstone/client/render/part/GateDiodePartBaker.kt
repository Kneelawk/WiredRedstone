package com.kneelawk.wiredredstone.client.render.part

import alexiil.mc.lib.multipart.api.render.PartRenderContext
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import com.kneelawk.wiredredstone.WRConstants
import com.kneelawk.wiredredstone.client.render.*
import com.kneelawk.wiredredstone.part.key.GateDiodePartKey
import com.kneelawk.wiredredstone.util.ConnectionUtils
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh
import net.minecraft.util.Identifier
import java.util.function.Consumer

object GateDiodePartBaker : WRPartBaker<GateDiodePartKey> {
    private val BACKGROUND = WRConstants.id("block/gate_diode/background")
    private val INPUT_ON = WRConstants.id("block/gate_diode/redstone_input_on")
    private val INPUT_OFF = WRConstants.id("block/gate_diode/redstone_input_off")
    private val OUTPUT_ON = WRConstants.id("block/gate_diode/redstone_output_on")
    private val OUTPUT_OFF = WRConstants.id("block/gate_diode/redstone_output_off")

    private val cache: LoadingCache<GateDiodePartKey, Mesh> =
        CacheBuilder.newBuilder().build(CacheLoader.from(::makeMesh))

    private fun makeMesh(key: GateDiodePartKey): Mesh {
        val outputWireSpriteId =
            if (key.outputPowered) WRSprites.RED_ALLOY_WIRE_POWERED_ID else WRSprites.RED_ALLOY_WIRE_UNPOWERED_ID
        val inputWireSpriteId =
            if (key.inputPowered) WRSprites.RED_ALLOY_WIRE_POWERED_ID else WRSprites.RED_ALLOY_WIRE_UNPOWERED_ID

        val outputWireSprite = RenderUtils.getBlockSprite(outputWireSpriteId)
        val inputWireSprite = RenderUtils.getBlockSprite(inputWireSpriteId)

        val outputModelId = if (key.outputPowered) OUTPUT_ON else OUTPUT_OFF
        val inputModelId = if (key.inputPowered) INPUT_ON else INPUT_OFF

        val backgroundModel = RenderUtils.getModel(BACKGROUND)
        val outputModel = RenderUtils.getModel(outputModelId)
        val inputModel = RenderUtils.getModel(inputModelId)

        val outputMaterial = if (key.outputPowered) WRMaterials.POWERED_MATERIAL else WRMaterials.UNPOWERED_MATERIAL
        val inputMaterial = if (key.inputPowered) WRMaterials.POWERED_MATERIAL else WRMaterials.UNPOWERED_MATERIAL

        val builder = RenderUtils.MESH_BUILDER
        val emitter = TransformingQuadEmitter.Multi(
            builder.emitter, arrayOf(RotateQuadTransform(key.direction), SideQuadTransform(key.side))
        )

        RenderUtils.fromVanilla(backgroundModel, emitter, WRMaterials.UNPOWERED_MATERIAL)
        RenderUtils.fromVanilla(outputModel, emitter, outputMaterial)
        RenderUtils.fromVanilla(inputModel, emitter, inputMaterial)

        // render outer wire connections
        val conn = ConnectionUtils.unrotatedConnections(key.connections, key.direction)
        WireRendering.emitNorthWireCorner(
            conn, key.side.axis, key.direction.axis, 2f, 2f, outputWireSprite, 7f / 16f, outputMaterial, emitter
        )
        WireRendering.emitSouthWireCorner(
            conn, key.side.axis, key.direction.axis, 2f, 2f, inputWireSprite, 7f / 16f, inputMaterial, emitter
        )

        return builder.build()
    }

    override fun emitQuads(key: GateDiodePartKey, ctx: PartRenderContext) {
        ctx.meshConsumer().accept(cache[key])
    }

    override fun registerModels(out: Consumer<Identifier>) {
        out.accept(BACKGROUND)
        out.accept(INPUT_ON)
        out.accept(INPUT_OFF)
        out.accept(OUTPUT_ON)
        out.accept(OUTPUT_OFF)
    }
}