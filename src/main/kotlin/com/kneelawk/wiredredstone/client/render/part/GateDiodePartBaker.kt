package com.kneelawk.wiredredstone.client.render.part

import com.kneelawk.wiredredstone.WRConstants
import com.kneelawk.wiredredstone.WRConstants.overlay
import com.kneelawk.wiredredstone.client.render.*
import com.kneelawk.wiredredstone.part.key.GateDiodePartKey
import com.kneelawk.wiredredstone.util.ConnectionUtils
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier
import java.util.function.Consumer

object GateDiodePartBaker : AbstractPartBaker<GateDiodePartKey>() {
    private val BACKGROUND = WRConstants.id("block/gate_diode/background")
    private val INPUT_ON = WRConstants.id("block/gate_diode/redstone_input_on")
    private val INPUT_OFF = WRConstants.id("block/gate_diode/redstone_input_off")
    private val OUTPUT_ON = WRConstants.id("block/gate_diode/redstone_output_on")
    private val OUTPUT_OFF = WRConstants.id("block/gate_diode/redstone_output_off")

    override fun makeMesh(key: GateDiodePartKey): Mesh {
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
            conn, key.side, key.direction.axis, 2f / 16f, 2f / 16f, outputWireSprite, 7f / 16f, outputMaterial, emitter
        )
        WireRendering.emitSouthWireCorner(
            conn, key.side, key.direction.axis, 2f / 16f, 2f / 16f, inputWireSprite, 7f / 16f, inputMaterial, emitter
        )

        return builder.build()
    }

    override fun registerModels(out: Consumer<Identifier>) {
        out.accept(BACKGROUND)
        out.accept(INPUT_ON)
        out.accept(INPUT_OFF)
        out.accept(OUTPUT_ON)
        out.accept(OUTPUT_OFF)
    }

    override fun renderOverlayText(
        key: GateDiodePartKey, tr: TextRenderer, stack: MatrixStack, provider: VertexConsumerProvider, light: Int
    ) {
        RenderUtils.renderPortText(
            overlay("gate_diode.out").asOrderedText(), key.side, key.direction, 2.0 / 16.0, tr, stack, provider, light
        )
        RenderUtils.renderPortText(
            overlay("gate_diode.in").asOrderedText(), key.side, key.direction.opposite, 2.0 / 16.0, tr, stack, provider,
            light
        )
    }
}
