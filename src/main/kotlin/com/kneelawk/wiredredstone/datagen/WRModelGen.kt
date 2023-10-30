package com.kneelawk.wiredredstone.datagen

import com.kneelawk.wiredredstone.WRConstants.id
import com.kneelawk.wiredredstone.datagen.gate.gate
import com.kneelawk.wiredredstone.datagen.gate.o
import com.kneelawk.wiredredstone.item.WRItems
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider
import net.minecraft.data.client.ItemModelGenerator
import net.minecraft.data.client.model.*
import net.minecraft.item.Item
import net.minecraft.util.DyeColor
import net.minecraft.util.math.Vec3d
import java.util.Optional

class WRModelGen(output: FabricDataOutput) : FabricModelProvider(output) {
    companion object {
        private val LOWER = TextureKey.of("lower")
        private val UPPER = TextureKey.of("upper")

        private val STANDING_WIRE_MODEL = Model(
            Optional.of(id("item/standing_insulated_wire_template")), Optional.empty(), TextureKey.PARTICLE,
            TextureKey.CROSS, TextureKey.END
        )
        private val STANDING_BUNDLED_CABLE_MODEL = Model(
            Optional.of(id("item/standing_bundled_cable_template")),
            Optional.empty(),
            TextureKey.PARTICLE,
            TextureKey.TOP,
            LOWER,
            UPPER,
            TextureKey.BOTTOM,
            TextureKey.END
        )
    }

    override fun generateBlockStateModels(gen: BlockStateModelGenerator) {
        gen.gate("gate_and", id("block/gate_and")) {
            particle = "particle".o
            background("background".o) {
                torchBase(Vec3d(8.0, 2.0, 3.0))
                torchBase(Vec3d(8.0, 2.0, 9.0))
                torchBase(Vec3d(11.0, 2.0, 9.0))
                torchBase(Vec3d(5.0, 2.0, 9.0))
            }
            torch("torch_output", Vec3d(8.0, 2.0, 3.0))
            torch("torch_input_back", Vec3d(8.0, 2.0, 9.0), itemOn = true)
            torch("torch_input_right", Vec3d(11.0, 2.0, 9.0), itemOn = true)
            torch("torch_input_left", Vec3d(5.0, 2.0, 9.0), itemOn = true)
            redstone("redstone_anode", "redstone_anode".o, itemOn = true)
            redstone("redstone_input_back", "redstone_input_back".o, disableable = true)
            redstone("redstone_input_right", "redstone_input_right".o, disableable = true)
            redstone("redstone_input_left", "redstone_input_left".o, disableable = true)
        }

        gen.gate("gate_or", id("block/gate_or")) {
            particle = "particle".o
            background("background".o) {
                torchBase(Vec3d(8.0, 2.0, 3.0))
                torchBase(Vec3d(8.0, 2.0, 9.0))
            }
            torch("torch_output", Vec3d(8.0, 2.0, 3.0))
            torch("torch_input", Vec3d(8.0, 2.0, 9.0), itemOn = true)
            redstone("redstone_anode", "redstone_anode".o, itemOn = true)
            redstone("redstone_input_back", "redstone_input_back".o, disableable = true)
            redstone("redstone_input_right", "redstone_input_right".o, disableable = true)
            redstone("redstone_input_left", "redstone_input_left".o, disableable = true)
        }

        gen.gate("gate_xor", id("block/gate_xor")) {
            particle = "particle".o
            background("background".o) {
                torchBase(Vec3d(5.0, 2.0, 8.0))
                torchBase(Vec3d(11.0, 2.0, 8.0))
                torchBase(Vec3d(8.0, 2.0, 13.0))
            }
            torch("torch_input_left", Vec3d(5.0, 2.0, 8.0))
            torch("torch_input_right", Vec3d(11.0, 2.0, 8.0))
            torch("torch_bottom", Vec3d(8.0, 2.0, 13.0), itemOn = true)
            redstone("redstone_input_left", "redstone_input_left".o)
            redstone("redstone_input_right", "redstone_input_right".o)
            redstone("redstone_anode_bottom", "redstone_anode_bottom".o, itemOn = true)
            redstone("redstone_output", "redstone_output".o)
        }

        gen.gate("gate_nand", id("block/gate_nand")) {
            particle = "particle".o
            background("background".o) {
                torchBase(Vec3d(8.0, 2.0, 9.0))
                torchBase(Vec3d(11.0, 2.0, 9.0))
                torchBase(Vec3d(5.0, 2.0, 9.0))
            }
            torch("torch_input_back", Vec3d(8.0, 2.0, 9.0), itemOn = true)
            torch("torch_input_right", Vec3d(11.0, 2.0, 9.0), itemOn = true)
            torch("torch_input_left", Vec3d(5.0, 2.0, 9.0), itemOn = true)
            redstone("redstone_output", "redstone_output".o, itemOn = true)
            redstone("redstone_input_back", "redstone_input_back".o, disableable = true)
            redstone("redstone_input_right", "redstone_input_right".o, disableable = true)
            redstone("redstone_input_left", "redstone_input_left".o, disableable = true)
        }

        gen.gate("gate_nor", id("block/gate_nor")) {
            particle = "particle".o
            background("background".o) {
                torchBase(Vec3d(8.0, 2.0, 9.0))
            }
            torch("torch", Vec3d(8.0, 2.0, 9.0), itemOn = true)
            redstone("redstone_output", "redstone_output".o, itemOn = true)
            redstone("redstone_input_back", "redstone_input_back".o, disableable = true)
            redstone("redstone_input_right", "redstone_input_right".o, disableable = true)
            redstone("redstone_input_left", "redstone_input_left".o, disableable = true)
        }

        gen.gate("gate_xnor", id("block/gate_xnor")) {
            particle = "particle".o
            background("background".o) {
                torchBase(Vec3d(5.0, 2.0, 8.0))
                torchBase(Vec3d(11.0, 2.0, 8.0))
                torchBase(Vec3d(8.0, 2.0, 13.0))
                torchBase(Vec3d(8.0, 2.0, 3.0))
            }
            torch("torch_input_left", Vec3d(5.0, 2.0, 8.0))
            torch("torch_input_right", Vec3d(11.0, 2.0, 8.0))
            torch("torch_bottom", Vec3d(8.0, 2.0, 13.0), itemOn = true)
            torch("torch_output", Vec3d(8.0, 2.0, 3.0), itemOn = true)
            redstone("redstone_input_left", "redstone_input_left".o)
            redstone("redstone_input_right", "redstone_input_right".o)
            redstone("redstone_anode_bottom", "redstone_anode_bottom".o, itemOn = true)
            redstone("redstone_anode_left", "redstone_anode_left".o)
            redstone("redstone_anode_right", "redstone_anode_right".o)
        }

        gen.gate("gate_diode", id("block/gate_diode")) {
            particle = "particle".o
            background("background".o)
            redstone("redstone_output", "redstone_output".o)
            redstone("redstone_input", "redstone_input".o)
        }

        gen.gate("gate_not", id("block/gate_not")) {
            particle = "particle".o
            background("background".o) {
                torchBase(Vec3d(8.0, 2.0, 9.0))
            }
            torch("torch", Vec3d(8.0, 2.0, 9.0), itemOn = true)
            redstone("redstone_output", "redstone_output".o, itemOn = true)
            redstone("redstone_input", "redstone_input".o)
        }

        gen.gate("gate_repeater", id("block/gate_repeater")) {
            particle = "particle".o
            background("background".o) {
                torchBase(Vec3d(8.0, 2.0, 2.0))
            }
            model("torch_input_base") {
                torchBase(Vec3d(3.0, 2.0, 9.0))
            }
            torch("torch_output", Vec3d(8.0, 2.0, 2.0))
            torch("torch_input", Vec3d(3.0, 2.0, 9.0), itemOn = true)
            redstone("redstone_anode", "redstone_anode".o, itemOn = true)
            redstone("redstone_input", "redstone_input".o)
        }

        gen.gate("gate_rs_latch", id("block/gate_rs_latch"), doFlipped = true) {
            particle = "particle".o
            background("background".o) {
                torchBase(Vec3d(8.0, 2.0, 3.0))
                torchBase(Vec3d(8.0, 2.0, 13.0))
            }
            torch("torch_set", Vec3d(8.0, 2.0, 3.0))
            torch("torch_reset", Vec3d(8.0, 2.0, 13.0), itemOn = true)
            redstone("redstone_anode_set", "redstone_anode_set".o)
            redstone("redstone_anode_reset", "redstone_anode_reset".o, itemOn = true)
            redstone("redstone_input_set", "redstone_input_set".o)
            redstone("redstone_input_reset", "redstone_input_reset".o)
        }

        gen.gate("gate_projector_simple", id("block/gate_projector_simple")) {
            particle = "particle".o
            background("background".o)
            model("torch_base") {
                torchBase(Vec3d(8.0, 2.0, 11.0), texture = id("block/projector_torch_off").o)
            }
            torchOff("torch_off", Vec3d(8.0, 2.0, 11.0), texture = id("block/projector_torch_off").o, addToItem = true)
            torchOn("torch_on", Vec3d(8.0, 2.0, 11.0), texture = id("block/projector_torch").o)
            redstone("redstone_input", "redstone_input".o)
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

        generateStandingBundledCableModel(gen, WRItems.STANDING_BUNDLED_CABLES[null]!!, "")
        for (color in DyeColor.values()) {
            generateStandingBundledCableModel(gen, WRItems.STANDING_BUNDLED_CABLES[color]!!, color.getName() + "/")
        }
    }

    private fun generateStandingBundledCableModel(gen: ItemModelGenerator, item: Item, dirname: String) {
        val textureId = id("block/standing_bundled_cable/$dirname")
        val topId = textureId.extendPath("top_cross")
        val lowerId = textureId.extendPath("lower_cross")
        val upperId = textureId.extendPath("upper_cross")
        val bottomId = textureId.extendPath("bottom_cross")
        val endId = textureId.extendPath("end")

        STANDING_BUNDLED_CABLE_MODEL.upload(
            ModelIds.getItemModelId(item),
            Texture()
                .put(TextureKey.PARTICLE, topId)
                .put(TextureKey.TOP, topId)
                .put(LOWER, lowerId)
                .put(UPPER, upperId)
                .put(TextureKey.BOTTOM, bottomId)
                .put(TextureKey.END, endId),
            gen.writer
        )
    }
}
