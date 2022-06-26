package com.kneelawk.wiredredstone.client.render.part

import com.kneelawk.wiredredstone.WRConstants.id
import com.kneelawk.wiredredstone.WRConstants.overlay
import com.kneelawk.wiredredstone.client.render.*
import com.kneelawk.wiredredstone.client.render.WRMaterials.POWERED_MATERIAL
import com.kneelawk.wiredredstone.client.render.WRMaterials.UNPOWERED_MATERIAL
import com.kneelawk.wiredredstone.client.render.WRSprites.RED_ALLOY_WIRE_POWERED_ID
import com.kneelawk.wiredredstone.client.render.WRSprites.RED_ALLOY_WIRE_UNPOWERED_ID
import com.kneelawk.wiredredstone.part.key.GateNotPartKey
import com.kneelawk.wiredredstone.util.ConnectionUtils
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier
import java.util.function.Consumer

object GateNotPartBaker : AbstractPartBaker<GateNotPartKey>() {
    private val BACKGROUND = id("block/gate_not/background")
    private val INPUT_ON = id("block/gate_not/redstone_input_on")
    private val INPUT_OFF = id("block/gate_not/redstone_input_off")
    private val OUTPUT_ON = id("block/gate_not/redstone_output_on")
    private val OUTPUT_OFF = id("block/gate_not/redstone_output_off")
    private val TORCH_ON = id("block/gate_not/torch_on")
    private val TORCH_OFF = id("block/gate_not/torch_off")

    override fun makeMesh(key: GateNotPartKey): Mesh {
        val outputWireSpriteId =
            if (key.outputPowered) RED_ALLOY_WIRE_POWERED_ID else RED_ALLOY_WIRE_UNPOWERED_ID
        val inputWireSpriteId =
            if (key.inputPowered) RED_ALLOY_WIRE_POWERED_ID else RED_ALLOY_WIRE_UNPOWERED_ID

        val outputWireSprite = RenderUtils.getBlockSprite(outputWireSpriteId)
        val inputWireSprite = RenderUtils.getBlockSprite(inputWireSpriteId)

        val outputModelId = if (key.outputPowered) OUTPUT_ON else OUTPUT_OFF
        val inputModelId = if (key.inputPowered) INPUT_ON else INPUT_OFF
        val torchModelId = if (key.torchPowered) TORCH_ON else TORCH_OFF

        val backgroundModel = RenderUtils.getModel(BACKGROUND)
        val outputModel = RenderUtils.getModel(outputModelId)
        val inputModel = RenderUtils.getModel(inputModelId)
        val torchModel = RenderUtils.getModel(torchModelId)

        val outputMaterial = if (key.outputPowered) POWERED_MATERIAL else UNPOWERED_MATERIAL
        val inputMaterial = if (key.inputPowered) POWERED_MATERIAL else UNPOWERED_MATERIAL
        val torchMaterial = if (key.torchPowered) POWERED_MATERIAL else UNPOWERED_MATERIAL

        val builder = RenderUtils.MESH_BUILDER
        val emitter = TransformingQuadEmitter.Multi(
            builder.emitter, arrayOf(RotateQuadTransform(key.direction), SideQuadTransform(key.side))
        )

        RenderUtils.fromVanilla(backgroundModel, emitter, UNPOWERED_MATERIAL)
        RenderUtils.fromVanilla(outputModel, emitter, outputMaterial)
        RenderUtils.fromVanilla(inputModel, emitter, inputMaterial)
        RenderUtils.fromVanilla(torchModel, emitter, torchMaterial)

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
        out.accept(TORCH_ON)
        out.accept(TORCH_OFF)
    }

    override fun renderOverlayText(
        key: GateNotPartKey, stack: MatrixStack, provider: VertexConsumerProvider, light: Int
    ) {
        RenderUtils.renderPortText(
            overlay("gate_not.out"), key.side, key.direction, 2.0 / 16.0, stack, provider, light
        )
        RenderUtils.renderPortText(
            overlay("gate_not.in"), key.side, key.direction.opposite, 2.0 / 16.0, stack, provider, light
        )
    }
}
