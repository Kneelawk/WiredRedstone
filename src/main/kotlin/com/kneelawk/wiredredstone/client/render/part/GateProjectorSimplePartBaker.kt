package com.kneelawk.wiredredstone.client.render.part

import com.kneelawk.wiredredstone.WRConstants.id
import com.kneelawk.wiredredstone.client.render.*
import com.kneelawk.wiredredstone.client.render.WRMaterials.POWERED_MATERIAL
import com.kneelawk.wiredredstone.client.render.WRMaterials.UNPOWERED_MATERIAL
import com.kneelawk.wiredredstone.client.render.WRSprites.RED_ALLOY_WIRE_POWERED_ID
import com.kneelawk.wiredredstone.client.render.WRSprites.RED_ALLOY_WIRE_UNPOWERED_ID
import com.kneelawk.wiredredstone.part.key.GateProjectorSimplePartKey
import com.kneelawk.wiredredstone.util.bits.ConnectionUtils
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec3f
import java.util.function.Consumer

object GateProjectorSimplePartBaker : AbstractPartBaker<GateProjectorSimplePartKey>() {
    private val BACKGROUND = id("block/gate_projector_simple/background")
    private val INPUT_OFF = id("block/gate_projector_simple/redstone_input_off")
    private val INPUT_ON = id("block/gate_projector_simple/redstone_input_on")
    private val TORCH_BASE = id("block/gate_projector_simple/torch_base")
    private val TORCH_OFF = id("block/gate_projector_simple/torch_off")
    private val TORCH_ON = id("block/gate_projector_simple/torch_on")

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
}
