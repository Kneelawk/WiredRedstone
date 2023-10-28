package com.kneelawk.wiredredstone.client.render.part

import com.kneelawk.wiredredstone.WRConstants.id
import com.kneelawk.wiredredstone.WRConstants.overlay
import com.kneelawk.wiredredstone.client.render.*
import com.kneelawk.wiredredstone.client.render.WRMaterials.POWERED_MATERIAL
import com.kneelawk.wiredredstone.client.render.WRMaterials.UNPOWERED_MATERIAL
import com.kneelawk.wiredredstone.part.key.GateXnorPartKey
import com.kneelawk.wiredredstone.util.RotationUtils.cardinalRotatedDirection
import com.kneelawk.wiredredstone.util.bits.ConnectionUtils
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier
import net.minecraft.util.math.Direction.EAST
import net.minecraft.util.math.Direction.WEST
import java.util.function.Consumer

object GateXnorPartBaker : AbstractPartBaker<GateXnorPartKey>() {
    private val BACKGROUND = id("block/gate_xnor/background")
    private val INPUT_RIGHT_ON = id("block/gate_xnor/redstone_input_right_on")
    private val INPUT_RIGHT_OFF = id("block/gate_xnor/redstone_input_right_off")
    private val INPUT_LEFT_ON = id("block/gate_xnor/redstone_input_left_on")
    private val INPUT_LEFT_OFF = id("block/gate_xnor/redstone_input_left_off")
    private val ANODE_BOTTOM_ON = id("block/gate_xnor/redstone_anode_bottom_on")
    private val ANODE_BOTTOM_OFF = id("block/gate_xnor/redstone_anode_bottom_off")
    private val ANODE_RIGHT_ON = id("block/gate_xnor/redstone_anode_right_on")
    private val ANODE_RIGHT_OFF = id("block/gate_xnor/redstone_anode_right_off")
    private val ANODE_LEFT_ON = id("block/gate_xnor/redstone_anode_left_on")
    private val ANODE_LEFT_OFF = id("block/gate_xnor/redstone_anode_left_off")
    private val TORCH_BOTTOM_ON = id("block/gate_xnor/torch_bottom_on")
    private val TORCH_BOTTOM_OFF = id("block/gate_xnor/torch_bottom_off")
    private val TORCH_RIGHT_ON = id("block/gate_xnor/torch_input_right_on")
    private val TORCH_RIGHT_OFF = id("block/gate_xnor/torch_input_right_off")
    private val TORCH_LEFT_ON = id("block/gate_xnor/torch_input_left_on")
    private val TORCH_LEFT_OFF = id("block/gate_xnor/torch_input_left_off")
    private val TORCH_OUTPUT_ON = id("block/gate_xnor/torch_output_on")
    private val TORCH_OUTPUT_OFF = id("block/gate_xnor/torch_output_off")

    override fun makeMesh(key: GateXnorPartKey): Mesh {
        val inputRightWireSpriteId =
            if (key.inputRightPowered) WRSprites.RED_ALLOY_WIRE_POWERED_ID else WRSprites.RED_ALLOY_WIRE_UNPOWERED_ID
        val inputLeftWireSpriteId = if (key.inputLeftPowered) WRSprites.RED_ALLOY_WIRE_POWERED_ID else WRSprites.RED_ALLOY_WIRE_UNPOWERED_ID
        val outputWireSpriteId = if (key.outputPowered) WRSprites.RED_ALLOY_WIRE_POWERED_ID else WRSprites.RED_ALLOY_WIRE_UNPOWERED_ID

        val inputRightWireSprite = RenderUtils.getBlockSprite(inputRightWireSpriteId)
        val inputLeftWireSprite = RenderUtils.getBlockSprite(inputLeftWireSpriteId)
        val outputWireSprite = RenderUtils.getBlockSprite(outputWireSpriteId)

        val inputRightModelId = if (key.inputRightPowered) INPUT_RIGHT_ON else INPUT_RIGHT_OFF
        val inputLeftModelId = if (key.inputLeftPowered) INPUT_LEFT_ON else INPUT_LEFT_OFF
        val anodeBottomModelId = if (!key.inputRightPowered && !key.inputLeftPowered) ANODE_BOTTOM_ON else ANODE_BOTTOM_OFF
        val anodeRightModelId = if (!key.inputRightPowered && key.inputLeftPowered) ANODE_RIGHT_ON else ANODE_RIGHT_OFF
        val anodeLeftModelId = if (!key.inputLeftPowered && key.inputRightPowered) ANODE_LEFT_ON else ANODE_LEFT_OFF
        val torchBottomModelId = if (!key.inputRightPowered && !key.inputLeftPowered) TORCH_BOTTOM_ON else TORCH_BOTTOM_OFF
        val torchRightModelId = if (!key.inputRightPowered && key.inputLeftPowered) TORCH_RIGHT_ON else TORCH_RIGHT_OFF
        val torchLeftModelId = if (!key.inputLeftPowered && key.inputRightPowered) TORCH_LEFT_ON else TORCH_LEFT_OFF
        val torchOutputModelId = if (key.outputPowered) TORCH_OUTPUT_ON else TORCH_OUTPUT_OFF

        val backgroundModel = RenderUtils.getModel(BACKGROUND)
        val inputRightModel = RenderUtils.getModel(inputRightModelId)
        val inputLeftModel = RenderUtils.getModel(inputLeftModelId)
        val anodeBottomModel = RenderUtils.getModel(anodeBottomModelId)
        val anodeRightModel = RenderUtils.getModel(anodeRightModelId)
        val anodeLeftModel = RenderUtils.getModel(anodeLeftModelId)
        val torchBottomModel = RenderUtils.getModel(torchBottomModelId)
        val torchRightModel = RenderUtils.getModel(torchRightModelId)
        val torchLeftModel = RenderUtils.getModel(torchLeftModelId)
        val torchOutputModel = RenderUtils.getModel(torchOutputModelId)

        val outputMaterial = if (key.outputPowered) POWERED_MATERIAL else UNPOWERED_MATERIAL
        val inputRightMaterial = if (key.inputRightPowered) POWERED_MATERIAL else UNPOWERED_MATERIAL
        val inputLeftMaterial = if (key.inputLeftPowered) POWERED_MATERIAL else UNPOWERED_MATERIAL
        val anodeBottomMaterial = if (!key.inputRightPowered && !key.inputLeftPowered) POWERED_MATERIAL else UNPOWERED_MATERIAL
        val anodeRightMaterial = if (!key.inputRightPowered && key.inputLeftPowered) POWERED_MATERIAL else UNPOWERED_MATERIAL
        val anodeLeftMaterial = if (!key.inputLeftPowered && key.inputRightPowered) POWERED_MATERIAL else UNPOWERED_MATERIAL
        val torchBottomMaterial = if (!key.inputRightPowered && !key.inputLeftPowered) POWERED_MATERIAL else UNPOWERED_MATERIAL
        val torchRightMaterial = if (!key.inputRightPowered && key.inputLeftPowered) POWERED_MATERIAL else UNPOWERED_MATERIAL
        val torchLeftMaterial = if (!key.inputLeftPowered && key.inputRightPowered) POWERED_MATERIAL else UNPOWERED_MATERIAL
        val torchOutputMaterial = if (key.outputPowered) POWERED_MATERIAL else UNPOWERED_MATERIAL

        val builder = RenderUtils.MESH_BUILDER
        val emitter = TransformingQuadEmitter.Multi(
            builder.emitter, arrayOf(RotateQuadTransform(key.direction), SideQuadTransform(key.side))
        )

        RenderUtils.fromVanilla(backgroundModel, emitter, UNPOWERED_MATERIAL)
        RenderUtils.fromVanilla(inputRightModel, emitter, inputRightMaterial)
        RenderUtils.fromVanilla(inputLeftModel, emitter, inputLeftMaterial)
        RenderUtils.fromVanilla(anodeBottomModel, emitter, anodeBottomMaterial)
        RenderUtils.fromVanilla(anodeRightModel, emitter, anodeRightMaterial)
        RenderUtils.fromVanilla(anodeLeftModel, emitter, anodeLeftMaterial)
        RenderUtils.fromVanilla(torchBottomModel, emitter, torchBottomMaterial)
        RenderUtils.fromVanilla(torchRightModel, emitter, torchRightMaterial)
        RenderUtils.fromVanilla(torchLeftModel, emitter, torchLeftMaterial)
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
        out.accept(INPUT_LEFT_ON)
        out.accept(INPUT_LEFT_OFF)
        out.accept(ANODE_BOTTOM_ON)
        out.accept(ANODE_BOTTOM_OFF)
        out.accept(ANODE_RIGHT_ON)
        out.accept(ANODE_RIGHT_OFF)
        out.accept(ANODE_LEFT_ON)
        out.accept(ANODE_LEFT_OFF)
        out.accept(TORCH_BOTTOM_ON)
        out.accept(TORCH_BOTTOM_OFF)
        out.accept(TORCH_RIGHT_ON)
        out.accept(TORCH_RIGHT_OFF)
        out.accept(TORCH_LEFT_ON)
        out.accept(TORCH_LEFT_OFF)
        out.accept(TORCH_OUTPUT_ON)
        out.accept(TORCH_OUTPUT_OFF)
    }

    override fun renderOverlayText(key: GateXnorPartKey, stack: MatrixStack, provider: VertexConsumerProvider) {
        RenderUtils.renderPortText(
            overlay("gate_xnor.out"), key.side, key.direction, 2.0 / 16.0, stack, provider
        )
        RenderUtils.renderPortText(
            overlay("gate_xnor.in"), key.side, cardinalRotatedDirection(EAST, key.direction), 2.0 / 16.0, stack,
            provider
        )
        RenderUtils.renderPortText(
            overlay("gate_xnor.in"), key.side, cardinalRotatedDirection(WEST, key.direction), 2.0 / 16.0, stack,
            provider
        )
    }
}
