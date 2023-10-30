package com.kneelawk.wiredredstone.client.render.part

import com.kneelawk.wiredredstone.WRConstants.id
import com.kneelawk.wiredredstone.WRConstants.overlay
import com.kneelawk.wiredredstone.client.render.*
import com.kneelawk.wiredredstone.client.render.WRMaterials.POWERED_MATERIAL
import com.kneelawk.wiredredstone.client.render.WRMaterials.UNPOWERED_MATERIAL
import com.kneelawk.wiredredstone.client.render.WRSprites.RED_ALLOY_WIRE_POWERED_ID
import com.kneelawk.wiredredstone.client.render.WRSprites.RED_ALLOY_WIRE_UNPOWERED_ID
import com.kneelawk.wiredredstone.part.key.GateRSLatchPartKey
import com.kneelawk.wiredredstone.util.RotationUtils.cardinalRotatedDirection
import com.kneelawk.wiredredstone.util.bits.ConnectionUtils
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier
import net.minecraft.util.math.Direction
import java.util.function.Consumer

object GateRSLatchPartBaker : AbstractPartBaker<GateRSLatchPartKey>() {
    private val BACKGROUND = id("block/gate_rs_latch/background")
    private val ANODE_RESET_OFF = id("block/gate_rs_latch/redstone_anode_reset_off")
    private val ANODE_RESET_ON = id("block/gate_rs_latch/redstone_anode_reset_on")
    private val ANODE_SET_OFF = id("block/gate_rs_latch/redstone_anode_set_off")
    private val ANODE_SET_ON = id("block/gate_rs_latch/redstone_anode_set_on")
    private val INPUT_RESET_OFF = id("block/gate_rs_latch/redstone_input_reset_off")
    private val INPUT_RESET_ON = id("block/gate_rs_latch/redstone_input_reset_on")
    private val INPUT_SET_OFF = id("block/gate_rs_latch/redstone_input_set_off")
    private val INPUT_SET_ON = id("block/gate_rs_latch/redstone_input_set_on")
    private val TORCH_RESET_OFF = id("block/gate_rs_latch/torch_reset_off")
    private val TORCH_RESET_ON = id("block/gate_rs_latch/torch_reset_on")
    private val TORCH_SET_OFF = id("block/gate_rs_latch/torch_set_off")
    private val TORCH_SET_ON = id("block/gate_rs_latch/torch_set_on")
    private val ALT_BACKGROUND = id("block/gate_rs_latch_flipped/background")
    private val ALT_ANODE_RESET_OFF = id("block/gate_rs_latch_flipped/redstone_anode_reset_off")
    private val ALT_ANODE_RESET_ON = id("block/gate_rs_latch_flipped/redstone_anode_reset_on")
    private val ALT_ANODE_SET_OFF = id("block/gate_rs_latch_flipped/redstone_anode_set_off")
    private val ALT_ANODE_SET_ON = id("block/gate_rs_latch_flipped/redstone_anode_set_on")
    private val ALT_INPUT_RESET_OFF = id("block/gate_rs_latch_flipped/redstone_input_reset_off")
    private val ALT_INPUT_RESET_ON = id("block/gate_rs_latch_flipped/redstone_input_reset_on")
    private val ALT_INPUT_SET_OFF = id("block/gate_rs_latch_flipped/redstone_input_set_off")
    private val ALT_INPUT_SET_ON = id("block/gate_rs_latch_flipped/redstone_input_set_on")
    private val ALT_TORCH_RESET_OFF = id("block/gate_rs_latch_flipped/torch_reset_off")
    private val ALT_TORCH_RESET_ON = id("block/gate_rs_latch_flipped/torch_reset_on")
    private val ALT_TORCH_SET_OFF = id("block/gate_rs_latch_flipped/torch_set_off")
    private val ALT_TORCH_SET_ON = id("block/gate_rs_latch_flipped/torch_set_on")

    override fun makeMesh(key: GateRSLatchPartKey): Mesh {
        val outputSetWireSpriteId = if (key.outputSetPowered) RED_ALLOY_WIRE_POWERED_ID else RED_ALLOY_WIRE_UNPOWERED_ID
        val outputResetWireSpriteId =
            if (key.outputResetPowered) RED_ALLOY_WIRE_POWERED_ID else RED_ALLOY_WIRE_UNPOWERED_ID
        val inputSetWireSpriteId = if (key.inputSetPowered) RED_ALLOY_WIRE_POWERED_ID else RED_ALLOY_WIRE_UNPOWERED_ID
        val inputResetWireSpriteId = if (key.inputSetPowered) RED_ALLOY_WIRE_POWERED_ID else RED_ALLOY_WIRE_UNPOWERED_ID

        val outputSetWireSprite = RenderUtils.getBlockSprite(outputSetWireSpriteId)
        val outputResetWireSprite = RenderUtils.getBlockSprite(outputResetWireSpriteId)
        val inputSetWireSprite = RenderUtils.getBlockSprite(inputSetWireSpriteId)
        val inputResetWireSprite = RenderUtils.getBlockSprite(inputResetWireSpriteId)

        val backgroundModelId = if (key.flipped) ALT_BACKGROUND else BACKGROUND
        val torchSetModelId =
            if (key.latchSetState && key.outputEnabled) if (key.flipped) ALT_TORCH_SET_ON else TORCH_SET_ON else if (key.flipped) ALT_TORCH_SET_OFF else TORCH_SET_OFF
        val torchResetModelId =
            if (!key.latchSetState && key.outputEnabled) if (key.flipped) ALT_TORCH_RESET_ON else TORCH_RESET_ON else if (key.flipped) ALT_TORCH_RESET_OFF else TORCH_RESET_OFF
        val anodeSetModelId =
            if (key.latchSetState && key.outputEnabled) if (key.flipped) ALT_ANODE_SET_ON else ANODE_SET_ON else if (key.flipped) ALT_ANODE_SET_OFF else ANODE_SET_OFF
        val anodeResetModelId =
            if (!key.latchSetState && key.outputEnabled) if (key.flipped) ALT_ANODE_RESET_ON else ANODE_RESET_ON else if (key.flipped) ALT_ANODE_RESET_OFF else ANODE_RESET_OFF
        val inputSetModelId =
            if (key.inputSetPowered) if (key.flipped) ALT_INPUT_SET_ON else INPUT_SET_ON else if (key.flipped) ALT_INPUT_SET_OFF else INPUT_SET_OFF
        val inputResetModelId =
            if (key.inputResetPowered) if (key.flipped) ALT_INPUT_RESET_ON else INPUT_RESET_ON else if (key.flipped) ALT_INPUT_RESET_OFF else INPUT_RESET_OFF

        val backgroundModel = RenderUtils.getModel(backgroundModelId)
        val torchSetModel = RenderUtils.getModel(torchSetModelId)
        val torchResetModel = RenderUtils.getModel(torchResetModelId)
        val anodeSetModel = RenderUtils.getModel(anodeSetModelId)
        val anodeResetModel = RenderUtils.getModel(anodeResetModelId)
        val inputSetModel = RenderUtils.getModel(inputSetModelId)
        val inputResetModel = RenderUtils.getModel(inputResetModelId)

        val outputSetMaterial = if (key.outputSetPowered) POWERED_MATERIAL else UNPOWERED_MATERIAL
        val outputResetMaterial = if (key.outputResetPowered) POWERED_MATERIAL else UNPOWERED_MATERIAL
        val torchSetMaterial = if (key.latchSetState && key.outputEnabled) POWERED_MATERIAL else UNPOWERED_MATERIAL
        val torchResetMaterial = if (!key.latchSetState && key.outputEnabled) POWERED_MATERIAL else UNPOWERED_MATERIAL
        val anodeSetMaterial = if (key.latchSetState && key.outputEnabled) POWERED_MATERIAL else UNPOWERED_MATERIAL
        val anodeResetMaterial = if (!key.latchSetState && key.outputEnabled) POWERED_MATERIAL else UNPOWERED_MATERIAL
        val inputSetMaterial = if (key.inputSetPowered) POWERED_MATERIAL else UNPOWERED_MATERIAL
        val inputResetMaterial = if (key.inputResetPowered) POWERED_MATERIAL else UNPOWERED_MATERIAL

        val builder = RenderUtils.MESH_BUILDER
        val emitter = TransformingQuadEmitter.Multi(
            builder.emitter, arrayOf(RotateQuadTransform(key.direction), SideQuadTransform(key.side))
        )

        RenderUtils.fromVanilla(backgroundModel, emitter, UNPOWERED_MATERIAL)
        RenderUtils.fromVanilla(torchSetModel, emitter, torchSetMaterial)
        RenderUtils.fromVanilla(torchResetModel, emitter, torchResetMaterial)
        RenderUtils.fromVanilla(anodeSetModel, emitter, anodeSetMaterial)
        RenderUtils.fromVanilla(anodeResetModel, emitter, anodeResetMaterial)
        RenderUtils.fromVanilla(inputSetModel, emitter, inputSetMaterial)
        RenderUtils.fromVanilla(inputResetModel, emitter, inputResetMaterial)

        // render outer wire connections
        val conn = ConnectionUtils.unrotatedConnections(key.connections, key.direction)
        WireRendering.emitNorthWireCorner(
            conn, key.side, key.direction.axis, 2f / 16f, 2f / 16f, outputSetWireSprite, 7f / 16f, outputSetMaterial,
            emitter
        )
        val inputResetSide = if (key.flipped) Direction.WEST else Direction.EAST
        WireRendering.emitWireCorner(
            inputResetSide, conn, key.side, key.direction.axis, 2f / 16f, 2f / 16f, inputResetWireSprite, 7f / 16f,
            inputResetMaterial, emitter
        )
        WireRendering.emitSouthWireCorner(
            conn, key.side, key.direction.axis, 2f / 16f, 2f / 16f, outputResetWireSprite, 7f / 16f,
            outputResetMaterial, emitter
        )
        WireRendering.emitWireCorner(
            inputResetSide.opposite, conn, key.side, key.direction.axis, 2f / 16f, 2f / 16f, inputSetWireSprite,
            7f / 16f, inputSetMaterial, emitter
        )

        return builder.build()
    }

    override fun registerModels(out: Consumer<Identifier>) {
        out.accept(BACKGROUND)
        out.accept(ANODE_RESET_OFF)
        out.accept(ANODE_RESET_ON)
        out.accept(ANODE_SET_OFF)
        out.accept(ANODE_SET_ON)
        out.accept(INPUT_RESET_OFF)
        out.accept(INPUT_RESET_ON)
        out.accept(INPUT_SET_OFF)
        out.accept(INPUT_SET_ON)
        out.accept(TORCH_RESET_OFF)
        out.accept(TORCH_RESET_ON)
        out.accept(TORCH_SET_OFF)
        out.accept(TORCH_SET_ON)
        out.accept(ALT_BACKGROUND)
        out.accept(ALT_ANODE_RESET_OFF)
        out.accept(ALT_ANODE_RESET_ON)
        out.accept(ALT_ANODE_SET_OFF)
        out.accept(ALT_ANODE_SET_ON)
        out.accept(ALT_INPUT_RESET_OFF)
        out.accept(ALT_INPUT_RESET_ON)
        out.accept(ALT_INPUT_SET_OFF)
        out.accept(ALT_INPUT_SET_ON)
        out.accept(ALT_TORCH_RESET_OFF)
        out.accept(ALT_TORCH_RESET_ON)
        out.accept(ALT_TORCH_SET_OFF)
        out.accept(ALT_TORCH_SET_ON)
    }

    override fun renderOverlayText(
        key: GateRSLatchPartKey, stack: MatrixStack, provider: VertexConsumerProvider
    ) {
        RenderUtils.renderPortText(
            overlay("gate_rs_latch.set_out"), key.side, cardinalRotatedDirection(Direction.NORTH, key.direction),
            2.0 / 16.0, stack, provider
        )
        RenderUtils.renderPortText(
            overlay("gate_rs_latch.reset_out"), key.side, cardinalRotatedDirection(Direction.SOUTH, key.direction),
            2.0 / 16.0, stack, provider, overline = true
        )
        RenderUtils.renderPortText(
            overlay("gate_rs_latch.set_in"), key.side,
            cardinalRotatedDirection(if (key.flipped) Direction.EAST else Direction.WEST, key.direction),
            2.0 / 16.0, stack, provider
        )
        RenderUtils.renderPortText(
            overlay("gate_rs_latch.reset_in"), key.side,
            cardinalRotatedDirection(if (key.flipped) Direction.WEST else Direction.EAST, key.direction),
            2.0 / 16.0, stack, provider
        )
    }
}
