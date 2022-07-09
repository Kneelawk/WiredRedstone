package com.kneelawk.wiredredstone.client.render.part

import com.kneelawk.wiredredstone.WRConstants
import com.kneelawk.wiredredstone.WRConstants.overlay
import com.kneelawk.wiredredstone.client.render.*
import com.kneelawk.wiredredstone.part.key.GateNorPartKey
import com.kneelawk.wiredredstone.util.ConnectionUtils
import com.kneelawk.wiredredstone.util.RotationUtils.cardinalRotatedDirection
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier
import net.minecraft.util.math.Direction.*
import java.util.function.Consumer

object GateNorPartBaker : AbstractPartBaker<GateNorPartKey>() {
    private val BACKGROUND = WRConstants.id("block/gate_nor/background")
    private val INPUT_RIGHT_ON = WRConstants.id("block/gate_nor/redstone_input_right_on")
    private val INPUT_RIGHT_OFF = WRConstants.id("block/gate_nor/redstone_input_right_off")
    private val INPUT_BACK_ON = WRConstants.id("block/gate_nor/redstone_input_back_on")
    private val INPUT_BACK_OFF = WRConstants.id("block/gate_nor/redstone_input_back_off")
    private val INPUT_LEFT_ON = WRConstants.id("block/gate_nor/redstone_input_left_on")
    private val INPUT_LEFT_OFF = WRConstants.id("block/gate_nor/redstone_input_left_off")
    private val OUTPUT_ON = WRConstants.id("block/gate_nor/redstone_output_on")
    private val OUTPUT_OFF = WRConstants.id("block/gate_nor/redstone_output_off")
    private val TORCH_ON = WRConstants.id("block/gate_nor/torch_on")
    private val TORCH_OFF = WRConstants.id("block/gate_nor/torch_off")

    override fun makeMesh(key: GateNorPartKey): Mesh {
        val outputWireSpriteId =
            if (key.outputPowered) WRSprites.RED_ALLOY_WIRE_POWERED_ID else WRSprites.RED_ALLOY_WIRE_UNPOWERED_ID
        val inputRightWireSpriteId =
            if (key.inputRightPowered) WRSprites.RED_ALLOY_WIRE_POWERED_ID else WRSprites.RED_ALLOY_WIRE_UNPOWERED_ID
        val inputBackWireSpriteId =
            if (key.inputBackPowered) WRSprites.RED_ALLOY_WIRE_POWERED_ID else WRSprites.RED_ALLOY_WIRE_UNPOWERED_ID
        val inputLeftWireSpriteId =
            if (key.inputLeftPowered) WRSprites.RED_ALLOY_WIRE_POWERED_ID else WRSprites.RED_ALLOY_WIRE_UNPOWERED_ID

        val outputWireSprite = RenderUtils.getBlockSprite(outputWireSpriteId)
        val inputRightWireSprite = RenderUtils.getBlockSprite(inputRightWireSpriteId)
        val inputBackWireSprite = RenderUtils.getBlockSprite(inputBackWireSpriteId)
        val inputLeftWireSprite = RenderUtils.getBlockSprite(inputLeftWireSpriteId)

        val outputModelId = if (key.outputPowered) OUTPUT_ON else OUTPUT_OFF
        val inputRightModelId = if (key.inputRightPowered) INPUT_RIGHT_ON else INPUT_RIGHT_OFF
        val inputBackModelId = if (key.inputBackPowered) INPUT_BACK_ON else INPUT_BACK_OFF
        val inputLeftModelId = if (key.inputLeftPowered) INPUT_LEFT_ON else INPUT_LEFT_OFF
        val torchModelId = if (key.torchPowered) TORCH_ON else TORCH_OFF

        val backgroundModel = RenderUtils.getModel(BACKGROUND)
        val outputModel = RenderUtils.getModel(outputModelId)
        val inputRightModel = RenderUtils.getModel(inputRightModelId)
        val inputBackModel = RenderUtils.getModel(inputBackModelId)
        val inputLeftModel = RenderUtils.getModel(inputLeftModelId)
        val torchModel = RenderUtils.getModel(torchModelId)

        val outputMaterial = if (key.outputPowered) WRMaterials.POWERED_MATERIAL else WRMaterials.UNPOWERED_MATERIAL
        val inputRightMaterial =
            if (key.inputRightPowered) WRMaterials.POWERED_MATERIAL else WRMaterials.UNPOWERED_MATERIAL
        val inputBackMaterial =
            if (key.inputBackPowered) WRMaterials.POWERED_MATERIAL else WRMaterials.UNPOWERED_MATERIAL
        val inputLeftMaterial =
            if (key.inputLeftPowered) WRMaterials.POWERED_MATERIAL else WRMaterials.UNPOWERED_MATERIAL
        val torchMaterial = if (key.torchPowered) WRMaterials.POWERED_MATERIAL else WRMaterials.UNPOWERED_MATERIAL

        val builder = RenderUtils.MESH_BUILDER
        val emitter = TransformingQuadEmitter.Multi(
            builder.emitter, arrayOf(RotateQuadTransform(key.direction), SideQuadTransform(key.side))
        )

        RenderUtils.fromVanilla(backgroundModel, emitter, WRMaterials.UNPOWERED_MATERIAL)
        RenderUtils.fromVanilla(outputModel, emitter, outputMaterial)
        RenderUtils.fromVanilla(inputRightModel, emitter, inputRightMaterial)
        RenderUtils.fromVanilla(inputBackModel, emitter, inputBackMaterial)
        RenderUtils.fromVanilla(inputLeftModel, emitter, inputLeftMaterial)
        RenderUtils.fromVanilla(torchModel, emitter, torchMaterial)

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
        out.accept(INPUT_BACK_ON)
        out.accept(INPUT_BACK_OFF)
        out.accept(INPUT_LEFT_ON)
        out.accept(INPUT_LEFT_OFF)
        out.accept(OUTPUT_ON)
        out.accept(OUTPUT_OFF)
        out.accept(TORCH_ON)
        out.accept(TORCH_OFF)
    }

    override fun renderOverlayText(
        key: GateNorPartKey, stack: MatrixStack, provider: VertexConsumerProvider, light: Int
    ) {
        RenderUtils.renderPortText(
            overlay("gate_nor.out"), key.side, key.direction, 2.0 / 16.0, stack, provider, light
        )
        RenderUtils.renderPortText(
            overlay("gate_nor.in"), key.side, cardinalRotatedDirection(EAST, key.direction), 2.0 / 16.0, stack,
            provider, light
        )
        RenderUtils.renderPortText(
            overlay("gate_nor.in"), key.side, cardinalRotatedDirection(SOUTH, key.direction), 2.0 / 16.0, stack,
            provider, light
        )
        RenderUtils.renderPortText(
            overlay("gate_nor.in"), key.side, cardinalRotatedDirection(WEST, key.direction), 2.0 / 16.0, stack,
            provider, light
        )
    }
}
