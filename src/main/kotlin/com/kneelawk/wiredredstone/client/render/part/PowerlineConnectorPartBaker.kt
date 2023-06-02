package com.kneelawk.wiredredstone.client.render.part

import com.kneelawk.wiredredstone.WRConstants.id
import com.kneelawk.wiredredstone.client.render.RenderUtils
import com.kneelawk.wiredredstone.client.render.SideQuadTransform
import com.kneelawk.wiredredstone.client.render.TransformingQuadEmitter
import com.kneelawk.wiredredstone.client.render.WRMaterials
import com.kneelawk.wiredredstone.part.key.PowerlineConnectorPartKey
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh
import net.minecraft.util.Identifier
import java.util.function.Consumer

object PowerlineConnectorPartBaker : AbstractPartBaker<PowerlineConnectorPartKey>() {
    private val MODEL = id("block/powerline_connector")

    override fun makeMesh(key: PowerlineConnectorPartKey): Mesh {
        val model = RenderUtils.getModel(MODEL)

        val builder = RenderUtils.MESH_BUILDER
        val emitter = TransformingQuadEmitter.Single(builder.emitter, SideQuadTransform(key.side))

        println("Baking: $model to $emitter")

        RenderUtils.fromVanilla(model, emitter, WRMaterials.UNPOWERED_MATERIAL)

        return builder.build()
    }

    override fun registerModels(out: Consumer<Identifier>) {
        out.accept(MODEL)
    }
}
