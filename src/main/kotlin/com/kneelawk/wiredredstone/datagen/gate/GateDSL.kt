package com.kneelawk.wiredredstone.datagen.gate

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.kneelawk.wiredredstone.WRConstants.id
import net.minecraft.data.client.model.BlockStateModelGenerator
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec3d

private val FLAT = id("block/flat")

private val GATE_BASE = id("block/gate_base")
private val REDSTONE_TORCH_OFF = id("block/redstone_torch_off")
private val REDSTONE_TORCH = id("block/redstone_torch")

sealed interface IdentifierOrString {
    fun toIdentifier(prefix: Identifier): Identifier
    fun extendPath(extension: String): IdentifierOrString

    data class Id(val id: Identifier) : IdentifierOrString {
        override fun toIdentifier(prefix: Identifier) = id
        override fun extendPath(extension: String) = id.extendPath(extension).o
    }

    data class Str(val str: String) : IdentifierOrString {
        override fun toIdentifier(prefix: Identifier): Identifier = prefix.extendPath(str)
        override fun extendPath(extension: String) = (str + extension).o
    }
}

val Identifier.o: IdentifierOrString
    get() = IdentifierOrString.Id(this)
val String.o: IdentifierOrString
    get() = IdentifierOrString.Str(this)

class GateBuilder(
    private val blockPath: Identifier, private val itemPath: Identifier, private val texturePrefix: Identifier,
    private val doFlipped: Boolean, private val gen: BlockStateModelGenerator
) {
    private val itemBuilder = ModelBuilderImpl(false, texturePrefix)
    var particle: IdentifierOrString = GATE_BASE.o

    fun finish(item: Boolean) {
        if (item) {
            itemBuilder.particle(particle)
            gen.modelCollector.accept(itemPath, itemBuilder::toJson)
        }
    }

    fun model(name: String, addToItem: Boolean = true, configure: ModelBuilder.() -> Unit) {
        val builder = ModelBuilderImpl(false, texturePrefix)
        builder.particle(particle)
        builder.configure()

        val backgroundPath = blockPath.extendPath("/$name")
        gen.modelCollector.accept(backgroundPath, builder::toJson)

        if (doFlipped) {
            val flippedBuilder = ModelBuilderImpl(true, texturePrefix.extendPath("_flipped"))
            flippedBuilder.particle(particle)
            flippedBuilder.configure()

            val flippedModelPath = blockPath.extendPath("_flipped/$name")
            gen.modelCollector.accept(flippedModelPath, flippedBuilder::toJson)
        }

        if (addToItem) {
            itemBuilder.configure()
        }
    }

    fun background(
        background: IdentifierOrString, height: Double = 2.0, addToItem: Boolean = true,
        configure: ModelBuilder.() -> Unit = {}
    ) {
        model("background", addToItem) {
            base(background, height)
            configure()
        }
    }

    fun torchOff(
        name: String, pos: Vec3d, height: Double = 1.0, texture: IdentifierOrString = REDSTONE_TORCH_OFF.o,
        addToItem: Boolean = false
    ) {
        model(name, addToItem) {
            torchOff(pos, height, texture)
        }
    }

    fun torchOn(
        name: String, pos: Vec3d, height: Double = 1.0, texture: IdentifierOrString = REDSTONE_TORCH.o,
        addToItem: Boolean = false
    ) {
        model(name, addToItem) {
            torchOn(pos, height, texture)
        }
    }

    fun torch(
        name: String, pos: Vec3d, height: Double = 1.0, onTexture: IdentifierOrString = REDSTONE_TORCH.o,
        offTexture: IdentifierOrString = REDSTONE_TORCH_OFF.o, itemOn: Boolean = false
    ) {
        torchOff("${name}_off", pos, height, offTexture, addToItem = !itemOn)
        torchOn("${name}_on", pos, height, onTexture, addToItem = itemOn)
    }

    fun surface(
        name: String, textureName: String, texture: IdentifierOrString, height: Double = 2.0, addToItem: Boolean = true,
        configure: ModelBuilder.() -> Unit = {}
    ) {
        model(name, addToItem) {
            surface(textureName, texture, height)
            configure()
        }
    }

    fun redstone(
        name: String, off: IdentifierOrString, on: IdentifierOrString, disabled: IdentifierOrString? = null,
        height: Double = 2.0, itemOn: Boolean = false
    ) {
        surface("${name}_off", name, off, height, addToItem = !itemOn)
        surface("${name}_on", name, on, height, addToItem = itemOn)
        if (disabled != null) {
            surface("${name}_disabled", name, disabled, height, addToItem = false)
        }
    }

    fun redstone(
        name: String, texture: IdentifierOrString, disableable: Boolean = false, height: Double = 2.0,
        itemOn: Boolean = false
    ) {
        redstone(
            name, texture.extendPath("_off"), texture.extendPath("_on"),
            if (disableable) texture.extendPath("_disabled") else null, height, itemOn = itemOn
        )
    }
}

fun BlockStateModelGenerator.gate(
    gateName: String, texturePrefix: Identifier, item: Boolean = true, doFlipped: Boolean = false,
    configure: GateBuilder.() -> Unit
) {
    GateBuilder(id("block/$gateName"), id("item/$gateName"), texturePrefix, doFlipped, this).apply(configure)
        .finish(item)
}

interface ModelBuilder {
    val flip: Boolean

    fun parent(parent: Identifier)

    fun texture(name: String, path: IdentifierOrString)

    fun element(element: Element)

    fun particle(particle: IdentifierOrString) {
        texture("particle", particle)
    }

    fun base(background: IdentifierOrString, height: Double = 2.0) {
        texture("base", GATE_BASE.o)
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

    fun surface(name: String, texture: IdentifierOrString, height: Double = 2.0) {
        texture(name, texture)

        element(
            Element(
                Vec3d.ZERO,
                Vec3d(16.0, height, 16.0),
                up = Face(0.0, 0.0, 16.0, 16.0, "#$name")
            )
        )
    }

    fun torchBase(pos: Vec3d, height: Double = 1.0, texture: IdentifierOrString = REDSTONE_TORCH_OFF.o) {
        texture("torch_base", texture)

        val newPos = if (flip) Vec3d(16.0 - pos.x, pos.y, pos.z) else pos

        val v1 = 9.0 + height
        element(
            Element(
                newPos.subtract(1.0, 0.0, 1.0),
                newPos.add(1.0, height, 1.0),
                north = Face(7.0, 9.0, 9.0, v1, "#torch_base"),
                south = Face(7.0, 9.0, 9.0, v1, "#torch_base"),
                west = Face(7.0, 9.0, 9.0, v1, "#torch_base"),
                east = Face(7.0, 9.0, 9.0, v1, "#torch_base"),
            )
        )
    }

    fun torchOff(pos: Vec3d, height: Double = 1.0, texture: IdentifierOrString = REDSTONE_TORCH_OFF.o) {
        texture("torch_off", texture)

        val newPos = if (flip) Vec3d(16.0 - pos.x, pos.y, pos.z) else pos

        element(
            Element(
                newPos.add(-1.0, height, -1.0),
                newPos.add(1.0, height + 3.0, 1.0),
                up = Face(7.0, 6.0, 9.0, 8.0, "#torch_off"),
                north = Face(7.0, 6.0, 9.0, 9.0, "#torch_off"),
                south = Face(7.0, 6.0, 9.0, 9.0, "#torch_off"),
                west = Face(7.0, 6.0, 9.0, 9.0, "#torch_off"),
                east = Face(7.0, 6.0, 9.0, 9.0, "#torch_off"),
            )
        )
    }

    fun torchOn(pos: Vec3d, height: Double = 1.0, texture: IdentifierOrString = REDSTONE_TORCH.o) {
        texture("torch_on", texture)

        val newPos = if (flip) Vec3d(16.0 - pos.x, pos.y, pos.z) else pos

        element(
            Element(
                newPos.add(-8.0, height, -1.0),
                newPos.add(8.0, height + 9.0, 1.0),
                north = Face(0.0, 0.0, 16.0, 9.0, "#torch_on"),
                south = Face(0.0, 0.0, 16.0, 9.0, "#torch_on"),
            )
        )
        element(
            Element(
                newPos.add(-1.0, height, -8.0),
                newPos.add(1.0, height + 9.0, 8.0),
                west = Face(0.0, 0.0, 16.0, 9.0, "#torch_on"),
                east = Face(0.0, 0.0, 16.0, 9.0, "#torch_on"),
            )
        )
        element(
            Element(
                newPos.add(-1.0, height, -1.0),
                newPos.add(1.0, height + 3.0, 1.0),
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

class ModelBuilderImpl(override val flip: Boolean, private val prefix: Identifier) : ModelBuilder {
    var parent: Identifier = FLAT
    val textures = mutableMapOf<String, Identifier>()
    val elements = mutableListOf<Element>()

    override fun parent(parent: Identifier) {
        this.parent = parent
    }

    override fun texture(name: String, path: IdentifierOrString) {
        textures[name] = path.toIdentifier(prefix.extendPath("/"))
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
