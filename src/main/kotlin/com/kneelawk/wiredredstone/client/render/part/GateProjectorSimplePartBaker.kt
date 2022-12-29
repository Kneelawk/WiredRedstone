package com.kneelawk.wiredredstone.client.render.part

import com.kneelawk.wiredredstone.WRConstants.id
import com.kneelawk.wiredredstone.WRConstants.overlay
import com.kneelawk.wiredredstone.client.render.*
import com.kneelawk.wiredredstone.client.render.WRMaterials.POWERED_MATERIAL
import com.kneelawk.wiredredstone.client.render.WRMaterials.UNPOWERED_MATERIAL
import com.kneelawk.wiredredstone.client.render.WRSprites.RED_ALLOY_WIRE_POWERED_ID
import com.kneelawk.wiredredstone.client.render.WRSprites.RED_ALLOY_WIRE_UNPOWERED_ID
import com.kneelawk.wiredredstone.part.key.GateProjectorSimplePartKey
import com.kneelawk.wiredredstone.util.FaceUtils
import com.kneelawk.wiredredstone.util.RotationUtils
import com.kneelawk.wiredredstone.util.bits.ConnectionUtils
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3f
import java.util.function.Consumer

object GateProjectorSimplePartBaker : AbstractPartBaker<GateProjectorSimplePartKey>(),
    ProjectorPartBaker<GateProjectorSimplePartKey> {
    private val BACKGROUND = id("block/gate_projector_simple/background")
    private val INPUT_OFF = id("block/gate_projector_simple/redstone_input_off")
    private val INPUT_ON = id("block/gate_projector_simple/redstone_input_on")
    private val TORCH_BASE = id("block/gate_projector_simple/torch_base")
    private val TORCH_OFF = id("block/gate_projector_simple/torch_off")
    private val TORCH_ON = id("block/gate_projector_simple/torch_on")

    private val PROJECTOR_TARGET = id("textures/block/projector_target.png")
    private val PROJECTOR_TARGET_HIGHLIGHT = id("textures/block/projector_target_highlight.png")

    override fun makeMesh(key: GateProjectorSimplePartKey): Mesh {
        val inputWireSpriteId =
            if (key.powered) RED_ALLOY_WIRE_POWERED_ID else RED_ALLOY_WIRE_UNPOWERED_ID
        val inputWireSprite = RenderUtils.getBlockSprite(inputWireSpriteId)

        val inputModelId = if (key.powered) INPUT_ON else INPUT_OFF
        val torchModelId = if (key.powered) TORCH_ON else TORCH_OFF

        val backgroundModel = RenderUtils.getModel(BACKGROUND)
        val torchBaseModel = RenderUtils.getModel(TORCH_BASE)
        val torchModel = RenderUtils.getModel(torchModelId)
        val inputModel = RenderUtils.getModel(inputModelId)

        val material = if (key.powered) POWERED_MATERIAL else UNPOWERED_MATERIAL

        val builder = RenderUtils.MESH_BUILDER
        val emitter0 = builder.emitter
        val emitter1 = TransformingQuadEmitter.Multi(
            emitter0, arrayOf(RotateQuadTransform(key.direction), SideQuadTransform(key.side))
        )
        val emitter2 = TransformingQuadEmitter.Multi(
            emitter0, arrayOf(
                TranslateQuadTransform(Vec3f(0f, 0f, -key.distance.toFloat() / 32f)),
                RotateQuadTransform(key.direction),
                SideQuadTransform(key.side)
            )
        )

        RenderUtils.fromVanilla(backgroundModel, emitter1, UNPOWERED_MATERIAL)
        RenderUtils.fromVanilla(torchBaseModel, emitter2, UNPOWERED_MATERIAL)
        RenderUtils.fromVanilla(torchModel, emitter2, material)
        RenderUtils.fromVanilla(inputModel, emitter1, material)

        // render outer wire connections
        val conn = ConnectionUtils.unrotatedConnections(key.connections, key.direction)
        WireRendering.emitSouthWireCorner(
            conn, key.side, key.direction.axis, 2f / 16f, 2f / 16f, inputWireSprite, 7f / 16f, material, emitter1
        )

        return builder.build()
    }

    override fun registerModels(out: Consumer<Identifier>) {
        out.accept(BACKGROUND)
        out.accept(INPUT_OFF)
        out.accept(INPUT_ON)
        out.accept(TORCH_BASE)
        out.accept(TORCH_OFF)
        out.accept(TORCH_ON)
    }

    override fun renderOverlayText(
        key: GateProjectorSimplePartKey, stack: MatrixStack, provider: VertexConsumerProvider
    ) {
        RenderUtils.renderPortText(
            overlay("gate_projector_simple.in"), key.side, key.direction.opposite, 2.0 / 16.0, stack, provider
        )
        RenderUtils.renderOverlayText(
            overlay("gate_projector_simple.distance", key.distance), key.side, key.direction, 0.5, 2.0 / 16.0,
            6.0 / 16.0, HorizontalAlignment.CENTER, stack, provider
        )

        renderProjectorTarget(key, stack, provider)
    }

    override fun renderProjectorTarget(
        key: GateProjectorSimplePartKey, stack: MatrixStack, provider: VertexConsumerProvider
    ) {
        val outputEdge = RotationUtils.rotatedDirection(key.side, key.direction)

        stack.push()
        stack.translate(
            (outputEdge.offsetX * key.distance).toDouble(), (outputEdge.offsetY * key.distance).toDouble(),
            (outputEdge.offsetZ * key.distance).toDouble()
        )

        val model = stack.peek().positionMatrix

        for (side in Direction.values()) {
            val face = FaceUtils.getFaceForSide(side)
            val tex = if (side == outputEdge) PROJECTOR_TARGET_HIGHLIGHT else PROJECTOR_TARGET
            val buf = provider.getBuffer(RenderLayer.getTextSeeThrough(tex))

            buf.vertex(model, face[0][0], face[0][1], face[0][2]).color(-1).texture(0f, 0f).light(15728880).next()
            buf.vertex(model, face[1][0], face[1][1], face[1][2]).color(-1).texture(0f, 1f).light(15728880).next()
            buf.vertex(model, face[2][0], face[2][1], face[2][2]).color(-1).texture(1f, 1f).light(15728880).next()
            buf.vertex(model, face[3][0], face[3][1], face[3][2]).color(-1).texture(1f, 0f).light(15728880).next()

            buf.vertex(model, face[3][0], face[3][1], face[3][2]).color(-1).texture(1f, 0f).light(15728880).next()
            buf.vertex(model, face[2][0], face[2][1], face[2][2]).color(-1).texture(1f, 1f).light(15728880).next()
            buf.vertex(model, face[1][0], face[1][1], face[1][2]).color(-1).texture(0f, 1f).light(15728880).next()
            buf.vertex(model, face[0][0], face[0][1], face[0][2]).color(-1).texture(0f, 0f).light(15728880).next()
        }

        stack.pop()
    }
}
