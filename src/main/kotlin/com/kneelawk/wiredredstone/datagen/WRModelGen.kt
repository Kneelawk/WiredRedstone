package com.kneelawk.wiredredstone.datagen

import com.kneelawk.wiredredstone.WRConstants.id
import com.kneelawk.wiredredstone.datagen.gate.gate
import com.kneelawk.wiredredstone.item.WRItems
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider
import net.minecraft.data.client.ItemModelGenerator
import net.minecraft.data.client.model.*
import net.minecraft.util.DyeColor
import net.minecraft.util.math.Vec3d
import java.util.Optional

class WRModelGen(output: FabricDataOutput) : FabricModelProvider(output) {
    companion object {
        private val STANDING_WIRE_MODEL = Model(
            Optional.of(id("item/standing_insulated_wire_template")), Optional.empty(), TextureKey.PARTICLE,
            TextureKey.CROSS, TextureKey.END
        )
    }

    override fun generateBlockStateModels(gen: BlockStateModelGenerator) {
        gen.gate("gate_and") {
            particle = id("block/gate_and/particle")
            background(id("block/gate_and/background")) {
                torchBase(Vec3d(8.0, 2.0, 3.0))
                torchBase(Vec3d(8.0, 2.0, 9.0))
                torchBase(Vec3d(11.0, 2.0, 9.0))
                torchBase(Vec3d(5.0, 2.0, 9.0))
            }
            torch("torch_output", Vec3d(8.0, 2.0, 3.0))
            torch("torch_input_back", Vec3d(8.0, 2.0, 9.0))
            torch("torch_input_right", Vec3d(11.0, 2.0, 9.0))
            torch("torch_input_left", Vec3d(5.0, 2.0, 9.0))
            redstone("redstone_anode", id("block/gate_and/redstone_anode"))
            redstone("redstone_input_back", id("block/gate_and/redstone_input_back"), disableable = true)
            redstone("redstone_input_right", id("block/gate_and/redstone_input_right"), disableable = true)
            redstone("redstone_input_left", id("block/gate_and/redstone_input_left"), disableable = true)
        }

        gen.gate("gate_or") {
            particle = id("block/gate_or/particle")
            background(id("block/gate_or/background")) {
                torchBase(Vec3d(8.0, 2.0, 3.0))
                torchBase(Vec3d(8.0, 2.0, 9.0))
            }
            torch("torch_output", Vec3d(8.0, 2.0, 3.0))
            torch("torch_input", Vec3d(8.0, 2.0, 9.0))
            redstone("redstone_anode", id("block/gate_or/redstone_anode"))
            redstone("redstone_input_back", id("block/gate_or/redstone_input_back"), disableable = true)
            redstone("redstone_input_right", id("block/gate_or/redstone_input_right"), disableable = true)
            redstone("redstone_input_left", id("block/gate_or/redstone_input_left"), disableable = true)
        }

        gen.gate("gate_nand") {
            particle = id("block/gate_nand/particle")
            background(id("block/gate_nand/background")) {
                torchBase(Vec3d(8.0, 2.0, 9.0))
                torchBase(Vec3d(11.0, 2.0, 9.0))
                torchBase(Vec3d(5.0, 2.0, 9.0))
            }
            torch("torch_input_back", Vec3d(8.0, 2.0, 9.0))
            torch("torch_input_right", Vec3d(11.0, 2.0, 9.0))
            torch("torch_input_left", Vec3d(5.0, 2.0, 9.0))
            redstone("redstone_output", id("block/gate_nand/redstone_output"))
            redstone("redstone_input_back", id("block/gate_nand/redstone_input_back"), disableable = true)
            redstone("redstone_input_right", id("block/gate_nand/redstone_input_right"), disableable = true)
            redstone("redstone_input_left", id("block/gate_nand/redstone_input_left"), disableable = true)
        }

        gen.gate("gate_nor") {
            particle = id("block/gate_nor/particle")
            background(id("block/gate_nor/background")) {
                torchBase(Vec3d(8.0, 2.0, 9.0))
            }
            torch("torch", Vec3d(8.0, 2.0, 9.0))
            redstone("redstone_output", id("block/gate_nor/redstone_output"))
            redstone("redstone_input_back", id("block/gate_nor/redstone_input_back"), disableable = true)
            redstone("redstone_input_right", id("block/gate_nor/redstone_input_right"), disableable = true)
            redstone("redstone_input_left", id("block/gate_nor/redstone_input_left"), disableable = true)
        }

        gen.gate("gate_diode") {
            particle = id("block/gate_diode/particle")
            background(id("block/gate_diode/background"))
            redstone("redstone_output", id("block/gate_diode/redstone_output"))
            redstone("redstone_input", id("block/gate_diode/redstone_input"))
        }

        gen.gate("gate_not") {
            particle = id("block/gate_not/particle")
            background(id("block/gate_not/background")) {
                torchBase(Vec3d(8.0, 2.0, 9.0))
            }
            torch("torch", Vec3d(8.0, 2.0, 9.0))
            redstone("redstone_output", id("block/gate_not/redstone_output"))
            redstone("redstone_input", id("block/gate_not/redstone_input"))
        }

        gen.gate("gate_repeater") {
            particle = id("block/gate_repeater/particle")
            background(id("block/gate_repeater/background")) {
                torchBase(Vec3d(8.0, 2.0, 2.0))
            }
            model("torch_input_base") {
                torchBase(Vec3d(3.0, 2.0, 9.0))
            }
            torch("torch_output", Vec3d(8.0, 2.0, 2.0))
            torch("torch_input", Vec3d(3.0, 2.0, 9.0))
            redstone("redstone_anode", id("block/gate_repeater/redstone_anode"))
            redstone("redstone_input", id("block/gate_repeater/redstone_input"))
        }

        gen.gate("gate_rs_latch") {
            particle = id("block/gate_rs_latch/particle")
            background(id("block/gate_rs_latch/background")) {
                torchBase(Vec3d(8.0, 2.0, 3.0))
                torchBase(Vec3d(8.0, 2.0, 13.0))
            }
            torch("torch_set", Vec3d(8.0, 2.0, 3.0))
            torch("torch_reset", Vec3d(8.0, 2.0, 13.0))
            redstone("redstone_anode_set", id("block/gate_rs_latch/redstone_anode_set"))
            redstone("redstone_anode_reset", id("block/gate_rs_latch/redstone_anode_reset"))
            redstone("redstone_input_set", id("block/gate_rs_latch/redstone_input_set"))
            redstone("redstone_input_reset", id("block/gate_rs_latch/redstone_input_reset"))
        }

        gen.gate("gate_projector_simple") {
            particle = id("block/gate_projector_simple/particle")
            background(id("block/gate_projector_simple/background"))
            model("torch_base") {
                torchBase(Vec3d(8.0, 2.0, 11.0), texture = id("block/projector_torch_off"))
            }
            torchOff("torch_off", Vec3d(8.0, 2.0, 11.0), texture = id("block/projector_torch_off"))
            torchOn("torch_on", Vec3d(8.0, 2.0, 11.0), texture = id("block/projector_torch"))
            redstone("redstone_input", id("block/gate_projector_simple/redstone_input"))
        }
    }

    override fun generateItemModels(gen: ItemModelGenerator) {
        for (color in DyeColor.values()) {
            val item = WRItems.STANDING_INSULATED_WIRES[color]!!
            val textureId = id("block/standing_insulated_wire/${color.getName()}_")
            val crossId = textureId.extendPath("cross")
            val endId = textureId.extendPath("end")

            STANDING_WIRE_MODEL.upload(
                ModelIds.getItemModelId(item),
                Texture().put(TextureKey.PARTICLE, crossId).put(TextureKey.CROSS, crossId).put(TextureKey.END, endId),
                gen.writer
            )
        }
    }
}
