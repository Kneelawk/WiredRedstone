package com.kneelawk.wiredredstone.client.render.part

import com.kneelawk.wiredredstone.WRConstants.id
import com.kneelawk.wiredredstone.WRConstants.overlay
import com.kneelawk.wiredredstone.client.render.*
import com.kneelawk.wiredredstone.client.render.WRMaterials.POWERED_MATERIAL
import com.kneelawk.wiredredstone.client.render.WRMaterials.UNPOWERED_MATERIAL
import com.kneelawk.wiredredstone.client.render.WRSprites.RED_ALLOY_WIRE_POWERED_ID
import com.kneelawk.wiredredstone.client.render.WRSprites.RED_ALLOY_WIRE_UNPOWERED_ID
import com.kneelawk.wiredredstone.part.key.GateRepeaterPartKey
import com.kneelawk.wiredredstone.util.bits.ConnectionUtils
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec3f
import java.util.function.Consumer

object GateRepeaterPartBaker : AbstractPartBaker<GateRepeaterPartKey>() {
    private val BACKGROUND = id("block/gate_repeater/background")
    private val INPUT_ON = id("block/gate_repeater/redstone_input_on")
    private val INPUT_OFF = id("block/gate_repeater/redstone_input_off")
    private val ANODE_ON = id("block/gate_repeater/redstone_anode_on")
    private val ANODE_OFF = id("block/gate_repeater/redstone_anode_off")
    private val TORCH_INPUT_BASE = id("block/gate_repeater/torch_input_base")
    private val TORCH_INPUT_ON = id("block/gate_repeater/torch_input_on")
    private val TORCH_INPUT_OFF = id("block/gate_repeater/torch_input_off")
    private val TORCH_OUTPUT_ON = id("block/gate_repeater/torch_output_on")
    private val TORCH_OUTPUT_OFF = id("block/gate_repeater/torch_output_off")

    override fun makeMesh(key: GateRepeaterPartKey): Mesh {
        val outputWireSpriteId =
            if (key.outputPowered) RED_ALLOY_WIRE_POWERED_ID else RED_ALLOY_WIRE_UNPOWERED_ID
        val inputWireSpriteId =
            if (key.inputPowered) RED_ALLOY_WIRE_POWERED_ID else RED_ALLOY_WIRE_UNPOWERED_ID

        val outputWireSprite = RenderUtils.getBlockSprite(outputWireSpriteId)
        val inputWireSprite = RenderUtils.getBlockSprite(inputWireSpriteId)

        val inputModelId = if (key.inputPowered) INPUT_ON else INPUT_OFF
        val anodeModelId = if (key.outputTorch) ANODE_OFF else ANODE_ON
        val torchInputModelId = if (key.inputPowered) TORCH_INPUT_OFF else TORCH_INPUT_ON
        val torchOutputModelId = if (key.outputTorch) TORCH_OUTPUT_ON else TORCH_OUTPUT_OFF

        val backgroundModel = RenderUtils.getModel(BACKGROUND)
        val inputModel = RenderUtils.getModel(inputModelId)
        val anodeModel = RenderUtils.getModel(anodeModelId)
        val torchInputBaseModel = RenderUtils.getModel(TORCH_INPUT_BASE)
        val torchInputModel = RenderUtils.getModel(torchInputModelId)
        val torchOutputModel = RenderUtils.getModel(torchOutputModelId)

        val inputMaterial = if (key.inputPowered) POWERED_MATERIAL else UNPOWERED_MATERIAL
        val anodeMaterial = if (key.outputTorch) UNPOWERED_MATERIAL else POWERED_MATERIAL
        val torchInputMaterial = if (key.inputPowered) UNPOWERED_MATERIAL else POWERED_MATERIAL
        val torchOutputMaterial = if (key.outputTorch) POWERED_MATERIAL else UNPOWERED_MATERIAL
        val outputMaterial = if (key.outputPowered) POWERED_MATERIAL else UNPOWERED_MATERIAL

        val builder = RenderUtils.MESH_BUILDER
        val emitter = builder.emitter
        val emitter1 = TransformingQuadEmitter.Multi(
            emitter, arrayOf(RotateQuadTransform(key.direction), SideQuadTransform(key.side))
        )
        val emitter2 = TransformingQuadEmitter.Multi(
            emitter, arrayOf(
                TranslateQuadTransform(Vec3f(key.delay.toFloat() / 32f, 0f, 0f)), RotateQuadTransform(key.direction),
                SideQuadTransform(key.side)
            )
        )

        RenderUtils.fromVanilla(backgroundModel, emitter1, UNPOWERED_MATERIAL)
        RenderUtils.fromVanilla(inputModel, emitter1, inputMaterial)
        RenderUtils.fromVanilla(anodeModel, emitter1, anodeMaterial)
        RenderUtils.fromVanilla(torchInputBaseModel, emitter2, UNPOWERED_MATERIAL)
        RenderUtils.fromVanilla(torchInputModel, emitter2, torchInputMaterial)
        RenderUtils.fromVanilla(torchOutputModel, emitter1, torchOutputMaterial)

        // render outer wire connections
        val conn = ConnectionUtils.unrotatedConnections(key.connections, key.direction)
        WireRendering.emitNorthWireCorner(
            conn, key.side, key.direction.axis, 2f / 16f, 2f / 16f, outputWireSprite, 7f / 16f, outputMaterial, emitter1
        )
        WireRendering.emitSouthWireCorner(
            conn, key.side, key.direction.axis, 2f / 16f, 2f / 16f, inputWireSprite, 7f / 16f, inputMaterial, emitter1
        )

        return builder.build()
    }

    override fun registerModels(out: Consumer<Identifier>) {
        out.accept(BACKGROUND)
        out.accept(INPUT_ON)
        out.accept(INPUT_OFF)
        out.accept(ANODE_ON)
        out.accept(ANODE_OFF)
        out.accept(TORCH_INPUT_BASE)
        out.accept(TORCH_INPUT_ON)
        out.accept(TORCH_INPUT_OFF)
        out.accept(TORCH_OUTPUT_ON)
        out.accept(TORCH_OUTPUT_OFF)
    }

    override fun renderOverlayText(
        key: GateRepeaterPartKey, stack: MatrixStack, provider: VertexConsumerProvider, light: Int
    ) {
        RenderUtils.renderPortText(
            overlay("gate_repeater.out"), key.side, key.direction, 2.0 / 16.0, stack, provider, light
        )
        RenderUtils.renderPortText(
            overlay("gate_repeater.in"), key.side, key.direction.opposite, 2.0 / 16.0, stack, provider, light
        )
        RenderUtils.renderOverlayText(
            overlay("gate_repeater.delay", (key.delay.toFloat() + 1f) / 2f), key.side, key.direction, 0.5, 2.0 / 16.0,
            6.0 / 16.0, HorizontalAlignment.CENTER, stack, provider, light
        )
    }
}
