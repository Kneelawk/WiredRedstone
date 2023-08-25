package com.kneelawk.wiredredstone.datagen.gate

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.kneelawk.wiredredstone.WRConstants.id
import net.minecraft.data.client.BlockStateModelGenerator
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec3d

private val FLAT = id("block/flat")

private val GATE_BASE = id("block/gate_base")
private val REDSTONE_TORCH_OFF = id("block/redstone_torch_off")
private val REDSTONE_TORCH = id("block/redstone_torch")

class GateBuilder(private val path: Identifier, private val gen: BlockStateModelGenerator) {
    var particle: Identifier = GATE_BASE

    fun model(name: String, configure: ModelBuilder.() -> Unit) {
        val builder = ModelBuilderImpl()
        builder.particle(particle)
        builder.configure()

        val backgroundPath = path.withSuffixedPath("/$name")
        gen.modelCollector.accept(backgroundPath, builder::toJson)
    }

    fun background(background: Identifier, height: Double = 2.0, configure: ModelBuilder.() -> Unit = {}) {
        model("background") {
            base(background, height)
            configure()
        }
    }

    fun torchOff(name: String, pos: Vec3d, height: Double = 1.0, texture: Identifier = REDSTONE_TORCH_OFF) {
        model(name) {
            torchOff(pos, height, texture)
        }
    }

    fun torchOn(name: String, pos: Vec3d, height: Double = 1.0, texture: Identifier = REDSTONE_TORCH) {
        model(name) {
            torchOn(pos, height, texture)
        }
    }

    fun torch(name: String, pos: Vec3d, height: Double = 1.0) {
        torchOff("${name}_off", pos, height)
        torchOn("${name}_on", pos, height)
    }

    fun surface(
        name: String, textureName: String, texture: Identifier, height: Double = 2.0,
        configure: ModelBuilder.() -> Unit = {}
    ) {
        model(name) {
            surface(textureName, texture, height)
            configure()
        }
    }

    fun redstone(name: String, off: Identifier, on: Identifier, disabled: Identifier? = null, height: Double = 2.0) {
        surface("${name}_off", "redstone", off, height)
        surface("${name}_on", "redstone", on, height)
        if (disabled != null) {
            surface("${name}_disabled", "redstone", disabled, height)
        }
    }

    fun redstone(name: String, texture: Identifier, disableable: Boolean = false, height: Double = 2.0) {
        redstone(
            name, texture.withSuffixedPath("_off"), texture.withSuffixedPath("_on"),
            if (disableable) texture.withSuffixedPath("_disabled") else null, height
        )
    }
}

fun BlockStateModelGenerator.gate(gateName: String, configure: GateBuilder.() -> Unit) {
    val path = id("block/$gateName")
    GateBuilder(path, this).apply(configure)
}

interface ModelBuilder {
    fun parent(parent: Identifier)

    fun texture(name: String, path: Identifier)

    fun element(element: Element)

    fun particle(particle: Identifier) {
        texture("particle", particle)
    }

    fun base(background: Identifier, height: Double = 2.0) {
        texture("base", GATE_BASE)
        texture("background", background)

        val v0 = 16.0 - height
        element(
            Element(
                Vec3d.ZERO,
                Vec3d(16.0, height, 16.0),
                Face(0.0, 0.0, 16.0, 16.0, "#base"),
                Face(0.0, 0.0, 16.0, 16.0, "#background"),
                Face(0.0, v0, 16.0, 16.0, "#base"),
                Face(0.0, v0, 16.0, 16.0, "#base"),
                Face(0.0, v0, 16.0, 16.0, "#base"),
                Face(0.0, v0, 16.0, 16.0, "#base"),
            )
        )
    }

    fun surface(name: String, texture: Identifier, height: Double = 2.0) {
        texture(name, texture)

        element(
            Element(
                Vec3d.ZERO,
                Vec3d(16.0, height, 16.0),
                up = Face(0.0, 0.0, 16.0, 16.0, "#$name")
            )
        )
    }

    fun torchBase(pos: Vec3d, height: Double = 1.0, texture: Identifier = REDSTONE_TORCH_OFF) {
        texture("torch_base", texture)

        val v1 = 9.0 + height
        element(
            Element(
                pos.subtract(1.0, 0.0, 1.0),
                pos.add(1.0, height, 1.0),
                north = Face(7.0, 9.0, 9.0, v1, "#torch_base"),
                south = Face(7.0, 9.0, 9.0, v1, "#torch_base"),
                west = Face(7.0, 9.0, 9.0, v1, "#torch_base"),
                east = Face(7.0, 9.0, 9.0, v1, "#torch_base"),
            )
        )
    }

    fun torchOff(pos: Vec3d, height: Double = 1.0, texture: Identifier = REDSTONE_TORCH_OFF) {
        texture("torch_off", texture)

        element(
            Element(
                pos.add(-1.0, height, -1.0),
                pos.add(1.0, height + 3.0, 1.0),
                up = Face(7.0, 6.0, 9.0, 8.0, "#torch_off"),
                north = Face(7.0, 6.0, 9.0, 9.0, "#torch_off"),
                south = Face(7.0, 6.0, 9.0, 9.0, "#torch_off"),
                west = Face(7.0, 6.0, 9.0, 9.0, "#torch_off"),
                east = Face(7.0, 6.0, 9.0, 9.0, "#torch_off"),
            )
        )
    }

    fun torchOn(pos: Vec3d, height: Double = 1.0, texture: Identifier = REDSTONE_TORCH) {
        texture("torch_on", texture)

        element(
            Element(
                pos.add(-8.0, height, -1.0),
                pos.add(8.0, height + 9.0, 1.0),
                north = Face(0.0, 0.0, 16.0, 9.0, "#torch_on"),
                south = Face(0.0, 0.0, 16.0, 9.0, "#torch_on"),
            )
        )
        element(
            Element(
                pos.add(-1.0, height, -8.0),
                pos.add(1.0, height + 9.0, 8.0),
                west = Face(0.0, 0.0, 16.0, 9.0, "#torch_on"),
                east = Face(0.0, 0.0, 16.0, 9.0, "#torch_on"),
            )
        )
        element(
            Element(
                pos.add(-1.0, height, -1.0),
                pos.add(1.0, height + 3.0, 1.0),
                up = Face(7.0, 6.0, 9.0, 8.0, "#torch_on"),
            )
        )
    }
}

class Face(val u0: Double, val v0: Double, val u1: Double, val v1: Double, val texture: String) {
    fun toJson(): JsonObject {
        val o = JsonObject()
        o.add("uv", JsonArray().apply {
            add(u0)
            add(v0)
            add(u1)
            add(v1)
        })
        o.addProperty("texture", texture)
        return o
    }
}

class Element(
    val from: Vec3d, val to: Vec3d, val down: Face? = null, val up: Face? = null, val north: Face? = null,
    val south: Face? = null, val west: Face? = null, val east: Face? = null
) {
    fun toJson(): JsonObject {
        val o = JsonObject()
        o.add("from", JsonArray().apply {
            add(from.x)
            add(from.y)
            add(from.z)
        })
        o.add("to", JsonArray().apply {
            add(to.x)
            add(to.y)
            add(to.z)
        })
        o.add("faces", JsonObject().apply {
            up?.let { add("up", it.toJson()) }
            down?.let { add("down", it.toJson()) }
            north?.let { add("north", it.toJson()) }
            south?.let { add("south", it.toJson()) }
            west?.let { add("west", it.toJson()) }
            east?.let { add("east", it.toJson()) }
        })
        return o
    }
}

class ModelBuilderImpl : ModelBuilder {
    var parent: Identifier = FLAT
    val textures = mutableMapOf<String, Identifier>()
    val elements = mutableListOf<Element>()

    override fun parent(parent: Identifier) {
        this.parent = parent
    }

    override fun texture(name: String, path: Identifier) {
        textures[name] = path
    }

    override fun element(element: Element) {
        elements += element
    }

    fun toJson(): JsonObject {
        val o = JsonObject()
        o.addProperty("parent", parent.toString())
        if (textures.isNotEmpty()) {
            o.add("textures", JsonObject().apply {
                textures.forEach { (k, v) -> addProperty(k, v.toString()) }
            })
        }
        if (elements.isNotEmpty()) {
            o.add("elements", JsonArray().apply {
                elements.forEach { add(it.toJson()) }
            })
        }
        return o
    }
}
