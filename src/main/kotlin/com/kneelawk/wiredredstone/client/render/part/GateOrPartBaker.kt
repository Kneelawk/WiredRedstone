package com.kneelawk.wiredredstone.client.render.part

import com.kneelawk.wiredredstone.WRConstants.id
import com.kneelawk.wiredredstone.WRConstants.overlay
import com.kneelawk.wiredredstone.client.render.*
import com.kneelawk.wiredredstone.client.render.Colors.portText
import com.kneelawk.wiredredstone.client.render.WRMaterials.POWERED_MATERIAL
import com.kneelawk.wiredredstone.client.render.WRMaterials.UNPOWERED_MATERIAL
import com.kneelawk.wiredredstone.client.render.WRSprites.RED_ALLOY_WIRE_POWERED_ID
import com.kneelawk.wiredredstone.client.render.WRSprites.RED_ALLOY_WIRE_UNPOWERED_ID
import com.kneelawk.wiredredstone.part.key.GateOrPartKey
import com.kneelawk.wiredredstone.util.ConnectionUtils
import com.kneelawk.wiredredstone.util.RotationUtils.cardinalRotatedDirection
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier
import net.minecraft.util.math.Direction.*
import java.util.function.Consumer

object GateOrPartBaker : AbstractPartBaker<GateOrPartKey>() {
    private val BACKGROUND = id("block/gate_or/background")
    private val INPUT_RIGHT_ON = id("block/gate_or/redstone_input_right_on")
    private val INPUT_RIGHT_OFF = id("block/gate_or/redstone_input_right_off")
    private val INPUT_RIGHT_DISABLED = id("block/gate_or/redstone_input_right_disabled")
    private val INPUT_BACK_ON = id("block/gate_or/redstone_input_back_on")
    private val INPUT_BACK_OFF = id("block/gate_or/redstone_input_back_off")
    private val INPUT_BACK_DISABLED = id("block/gate_or/redstone_input_back_disabled")
    private val INPUT_LEFT_ON = id("block/gate_or/redstone_input_left_on")
    private val INPUT_LEFT_OFF = id("block/gate_or/redstone_input_left_off")
    private val INPUT_LEFT_DISABLED = id("block/gate_or/redstone_input_left_disabled")
    private val TORCH_INPUT_ON = id("block/gate_or/torch_input_on")
    private val TORCH_INPUT_OFF = id("block/gate_or/torch_input_off")
    private val ANODE_ON = id("block/gate_or/redstone_anode_on")
    private val ANODE_OFF = id("block/gate_or/redstone_anode_off")
    private val TORCH_OUTPUT_ON = id("block/gate_or/torch_output_on")
    private val TORCH_OUTPUT_OFF = id("block/gate_or/torch_output_off")

    override fun makeMesh(key: GateOrPartKey): Mesh {
        val inputRightWireSpriteId =
            if (key.inputRightPowered) RED_ALLOY_WIRE_POWERED_ID else RED_ALLOY_WIRE_UNPOWERED_ID
        val inputBackWireSpriteId = if (key.inputBackPowered) RED_ALLOY_WIRE_POWERED_ID else RED_ALLOY_WIRE_UNPOWERED_ID
        val inputLeftWireSpriteId = if (key.inputLeftPowered) RED_ALLOY_WIRE_POWERED_ID else RED_ALLOY_WIRE_UNPOWERED_ID
        val outputWireSpriteId = if (key.outputPowered) RED_ALLOY_WIRE_POWERED_ID else RED_ALLOY_WIRE_UNPOWERED_ID

        val inputRightWireSprite = RenderUtils.getBlockSprite(inputRightWireSpriteId)
        val inputBackWireSprite = RenderUtils.getBlockSprite(inputBackWireSpriteId)
        val inputLeftWireSprite = RenderUtils.getBlockSprite(inputLeftWireSpriteId)
        val outputWireSprite = RenderUtils.getBlockSprite(outputWireSpriteId)

        val inputRightModelId =
            if (key.inputRightEnabled) if (key.inputRightPowered) INPUT_RIGHT_ON else INPUT_RIGHT_OFF else INPUT_RIGHT_DISABLED
        val inputBackModelId =
            if (key.inputBackEnabled) if (key.inputBackPowered) INPUT_BACK_ON else INPUT_BACK_OFF else INPUT_BACK_DISABLED
        val inputLeftModelId =
            if (key.inputLeftEnabled) if (key.inputLeftPowered) INPUT_LEFT_ON else INPUT_LEFT_OFF else INPUT_LEFT_DISABLED
        val torchInputModelId = if (!key.outputPowered) TORCH_INPUT_ON else TORCH_INPUT_OFF
        val anodeModelId = if (!key.outputPowered) ANODE_ON else ANODE_OFF
        val torchOutputModelId = if (key.outputPowered) TORCH_OUTPUT_ON else TORCH_OUTPUT_OFF

        val backgroundModel = RenderUtils.getModel(BACKGROUND)
        val inputRightModel = RenderUtils.getModel(inputRightModelId)
        val inputBackModel = RenderUtils.getModel(inputBackModelId)
        val inputLeftModel = RenderUtils.getModel(inputLeftModelId)
        val torchInputModel = RenderUtils.getModel(torchInputModelId)
        val anodeModel = RenderUtils.getModel(anodeModelId)
        val torchOutputModel = RenderUtils.getModel(torchOutputModelId)

        val outputMaterial = if (key.outputPowered) POWERED_MATERIAL else UNPOWERED_MATERIAL
        val inputRightMaterial =
            if (key.inputRightPowered && key.inputRightEnabled) POWERED_MATERIAL else UNPOWERED_MATERIAL
        val inputBackMaterial =
            if (key.inputBackPowered && key.inputBackEnabled) POWERED_MATERIAL else UNPOWERED_MATERIAL
        val inputLeftMaterial =
            if (key.inputLeftPowered && key.inputLeftEnabled) POWERED_MATERIAL else UNPOWERED_MATERIAL
        val torchInputMaterial = if (!key.outputPowered) POWERED_MATERIAL else UNPOWERED_MATERIAL
        val anodeMaterial = if (!key.outputPowered) POWERED_MATERIAL else UNPOWERED_MATERIAL
        val torchOutputMaterial = if (key.outputPowered) POWERED_MATERIAL else UNPOWERED_MATERIAL

        val builder = RenderUtils.MESH_BUILDER
        val emitter = TransformingQuadEmitter.Multi(
            builder.emitter, arrayOf(RotateQuadTransform(key.direction), SideQuadTransform(key.side))
        )

        RenderUtils.fromVanilla(backgroundModel, emitter, UNPOWERED_MATERIAL)
        RenderUtils.fromVanilla(inputRightModel, emitter, inputRightMaterial)
        RenderUtils.fromVanilla(inputBackModel, emitter, inputBackMaterial)
        RenderUtils.fromVanilla(inputLeftModel, emitter, inputLeftMaterial)
        RenderUtils.fromVanilla(torchInputModel, emitter, torchInputMaterial)
        RenderUtils.fromVanilla(anodeModel, emitter, anodeMaterial)
        RenderUtils.fromVanilla(torchOutputModel, emitter, torchOutputMaterial)

        // render outer wire connections
        val conn = ConnectionUtils.unrotatedConnections(key.connections, key.direction)
        WireRendering.emitNorthWireCorner(
            conn, key.side, key.direction.axis, 2f / 16f, 2f / 16f, outputWireSprite, 7f / 16f, outputMaterial, emitter
        )
        WireRendering.emitEastWireCorner(
            conn, key.side, key.direction.axis, 2f / 16f, 2f / 16f, inputRightWireSprite, 7f / 16f, inputRightMaterial,
            emitter
        )
        WireRendering.emitSouthWireCorner(
            conn, key.side, key.direction.axis, 2f / 16f, 2f / 16f, inputBackWireSprite, 7f / 16f, inputBackMaterial,
            emitter
        )
        WireRendering.emitWestWireCorner(
            conn, key.side, key.direction.axis, 2f / 16f, 2f / 16f, inputLeftWireSprite, 7f / 16f, inputLeftMaterial,
            emitter
        )

        return builder.build()
    }

    override fun registerModels(out: Consumer<Identifier>) {
        out.accept(BACKGROUND)
        out.accept(INPUT_RIGHT_ON)
        out.accept(INPUT_RIGHT_OFF)
        out.accept(INPUT_RIGHT_DISABLED)
        out.accept(INPUT_BACK_ON)
        out.accept(INPUT_BACK_OFF)
        out.accept(INPUT_BACK_DISABLED)
        out.accept(INPUT_LEFT_ON)
        out.accept(INPUT_LEFT_OFF)
        out.accept(INPUT_LEFT_DISABLED)
        out.accept(TORCH_INPUT_ON)
        out.accept(TORCH_INPUT_OFF)
        out.accept(ANODE_ON)
        out.accept(ANODE_OFF)
        out.accept(TORCH_OUTPUT_ON)
        out.accept(TORCH_OUTPUT_OFF)
    }

    override fun renderOverlayText(
        key: GateOrPartKey, stack: MatrixStack, provider: VertexConsumerProvider, light: Int
    ) {
        RenderUtils.renderPortText(
            overlay("gate_or.out"), key.side, key.direction, 2.0 / 16.0, stack, provider, light
        )
        RenderUtils.renderPortText(
            overlay("gate_or.in"), key.side, cardinalRotatedDirection(EAST, key.direction), 2.0 / 16.0,
            stack, provider, light, portText(key.inputRightEnabled)
        )
        RenderUtils.renderPortText(
            overlay("gate_or.in"), key.side, cardinalRotatedDirection(SOUTH, key.direction), 2.0 / 16.0,
            stack, provider, light, portText(key.inputBackEnabled)
        )
        RenderUtils.renderPortText(
            overlay("gate_or.in"), key.side, cardinalRotatedDirection(WEST, key.direction), 2.0 / 16.0,
            stack, provider, light, portText(key.inputLeftEnabled)
        )
    }
}
