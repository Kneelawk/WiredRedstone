package com.kneelawk.wiredredstone.client.render.part

import alexiil.mc.lib.multipart.api.render.PartRenderContext
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import com.kneelawk.wiredredstone.WRConstants.id
import com.kneelawk.wiredredstone.client.render.*
import com.kneelawk.wiredredstone.part.key.BundledCablePartKey
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh
import net.minecraft.client.texture.Sprite
import net.minecraft.util.DyeColor.*
import net.minecraft.util.Identifier

object BundledCablePartBaker : WRPartBaker<BundledCablePartKey> {
    private val cache: LoadingCache<BundledCablePartKey, Mesh> =
        CacheBuilder.newBuilder().build(CacheLoader.from(::makeMesh))

    override fun invalidateCaches() {
        cache.invalidateAll()
    }

    private fun makeMesh(key: BundledCablePartKey): Mesh {
        val builder = RenderUtils.MESH_BUILDER
        val emitter = TransformingQuadEmitter.Single(builder.emitter, SideQuadTransform(key.side))

        val sprites = BUNDLED_CABLE_IDS[key.color]!!.lookup()

        val outerCornerLowerSide = RenderUtils.getBlockSprite(OUTER_CORNER_LOWER_SIDE)
        val outerCornerUpperSide = RenderUtils.getBlockSprite(OUTER_CORNER_UPPER_SIDE)
        val outerCornerTop = RenderUtils.getBlockSprite(OUTER_CORNER_TOP)
        val end = RenderUtils.getBlockSprite(END)

        WireRendering.emitWire(
            conn = key.connections,
            side = key.side,
            wireHeight = 4f / 16f,
            wireWidth = 6f / 16f,
            topCrossSprite = sprites.topCross,
            topXSprite = sprites.topX,
            topZSprite = sprites.topZ,
            bottomCrossSprite = sprites.bottomCross,
            bottomXSprite = sprites.bottomX,
            bottomZSprite = sprites.bottomZ,
            sideSprite = sprites.lowerSide,
            upperSideSprite = sprites.upperSide,
            cornerTopXSprite = outerCornerTop,
            cornerTopZSprite = outerCornerTop,
            cornerSideSprite = outerCornerLowerSide,
            cornerUpperSideSprite = outerCornerUpperSide,
            openEndSprite = end,
            closedEndSprite = outerCornerTop,
            sideV = 5f / 16f,
            material = WRMaterials.UNPOWERED_MATERIAL,
            emitter = emitter
        )

        return builder.build()
    }

    override fun emitQuads(key: BundledCablePartKey, ctx: PartRenderContext) {
        ctx.meshConsumer().accept(cache[key])
    }

    override fun registerSprites(registry: ClientSpriteRegistryCallback.Registry) {
        registry.register(OUTER_CORNER_LOWER_SIDE)
        registry.register(OUTER_CORNER_UPPER_SIDE)
        registry.register(OUTER_CORNER_TOP)

        for (wire in BUNDLED_CABLE_IDS.values) {
            wire.register(registry)
        }
    }

    data class WireIds(
        val topCross: Identifier, val topX: Identifier, val topZ: Identifier, val bottomCross: Identifier,
        val bottomX: Identifier, val bottomZ: Identifier, val lowerSide: Identifier, val upperSide: Identifier
    ) {
        fun register(registry: ClientSpriteRegistryCallback.Registry) {
            registry.register(topCross)
            registry.register(topX)
            registry.register(topZ)
            registry.register(bottomCross)
            registry.register(bottomX)
            registry.register(bottomZ)
            registry.register(lowerSide)
            registry.register(upperSide)
        }

        fun lookup(): WireSprites {
            return WireSprites(
                RenderUtils.getBlockSprite(topCross),
                RenderUtils.getBlockSprite(topX),
                RenderUtils.getBlockSprite(topZ),
                RenderUtils.getBlockSprite(bottomCross),
                RenderUtils.getBlockSprite(bottomX),
                RenderUtils.getBlockSprite(bottomZ),
                RenderUtils.getBlockSprite(lowerSide),
                RenderUtils.getBlockSprite(upperSide),
            )
        }
    }

    data class WireSprites(
        val topCross: Sprite, val topX: Sprite, val topZ: Sprite, val bottomCross: Sprite, val bottomX: Sprite,
        val bottomZ: Sprite, val lowerSide: Sprite, val upperSide: Sprite
    )

    private val OUTER_CORNER_LOWER_SIDE = id("block/bundled_cable/outer_corner_lower_side")
    private val OUTER_CORNER_UPPER_SIDE = id("block/bundled_cable/outer_corner_upper_side")
    private val OUTER_CORNER_TOP = id("block/bundled_cable/outer_corner_top")
    private val END = id("block/bundled_cable/end")

    private val BUNDLED_CABLE_IDS = mapOf(
        null to WireIds(
            id("block/bundled_cable/top_cross"),
            id("block/bundled_cable/top_x"),
            id("block/bundled_cable/top_z"),
            id("block/bundled_cable/bottom_cross"),
            id("block/bundled_cable/bottom_x"),
            id("block/bundled_cable/bottom_z"),
            id("block/bundled_cable/lower_side"),
            id("block/bundled_cable/upper_side"),
        ),
        WHITE to WireIds(
            id("block/bundled_cable/white/top_cross"),
            id("block/bundled_cable/white/top_x"),
            id("block/bundled_cable/white/top_z"),
            id("block/bundled_cable/white/bottom_cross"),
            id("block/bundled_cable/white/bottom_x"),
            id("block/bundled_cable/white/bottom_z"),
            id("block/bundled_cable/white/lower_side"),
            id("block/bundled_cable/white/upper_side"),
        ),
        ORANGE to WireIds(
            id("block/bundled_cable/orange/top_cross"),
            id("block/bundled_cable/orange/top_x"),
            id("block/bundled_cable/orange/top_z"),
            id("block/bundled_cable/orange/bottom_cross"),
            id("block/bundled_cable/orange/bottom_x"),
            id("block/bundled_cable/orange/bottom_z"),
            id("block/bundled_cable/orange/lower_side"),
            id("block/bundled_cable/orange/upper_side"),
        ),
        MAGENTA to WireIds(
            id("block/bundled_cable/magenta/top_cross"),
            id("block/bundled_cable/magenta/top_x"),
            id("block/bundled_cable/magenta/top_z"),
            id("block/bundled_cable/magenta/bottom_cross"),
            id("block/bundled_cable/magenta/bottom_x"),
            id("block/bundled_cable/magenta/bottom_z"),
            id("block/bundled_cable/magenta/lower_side"),
            id("block/bundled_cable/magenta/upper_side"),
        ),
        LIGHT_BLUE to WireIds(
            id("block/bundled_cable/light_blue/top_cross"),
            id("block/bundled_cable/light_blue/top_x"),
            id("block/bundled_cable/light_blue/top_z"),
            id("block/bundled_cable/light_blue/bottom_cross"),
            id("block/bundled_cable/light_blue/bottom_x"),
            id("block/bundled_cable/light_blue/bottom_z"),
            id("block/bundled_cable/light_blue/lower_side"),
            id("block/bundled_cable/light_blue/upper_side"),
        ),
        YELLOW to WireIds(
            id("block/bundled_cable/yellow/top_cross"),
            id("block/bundled_cable/yellow/top_x"),
            id("block/bundled_cable/yellow/top_z"),
            id("block/bundled_cable/yellow/bottom_cross"),
            id("block/bundled_cable/yellow/bottom_x"),
            id("block/bundled_cable/yellow/bottom_z"),
            id("block/bundled_cable/yellow/lower_side"),
            id("block/bundled_cable/yellow/upper_side"),
        ),
        LIME to WireIds(
            id("block/bundled_cable/lime/top_cross"),
            id("block/bundled_cable/lime/top_x"),
            id("block/bundled_cable/lime/top_z"),
            id("block/bundled_cable/lime/bottom_cross"),
            id("block/bundled_cable/lime/bottom_x"),
            id("block/bundled_cable/lime/bottom_z"),
            id("block/bundled_cable/lime/lower_side"),
            id("block/bundled_cable/lime/upper_side"),
        ),
        PINK to WireIds(
            id("block/bundled_cable/pink/top_cross"),
            id("block/bundled_cable/pink/top_x"),
            id("block/bundled_cable/pink/top_z"),
            id("block/bundled_cable/pink/bottom_cross"),
            id("block/bundled_cable/pink/bottom_x"),
            id("block/bundled_cable/pink/bottom_z"),
            id("block/bundled_cable/pink/lower_side"),
            id("block/bundled_cable/pink/upper_side"),
        ),
        GRAY to WireIds(
            id("block/bundled_cable/gray/top_cross"),
            id("block/bundled_cable/gray/top_x"),
            id("block/bundled_cable/gray/top_z"),
            id("block/bundled_cable/gray/bottom_cross"),
            id("block/bundled_cable/gray/bottom_x"),
            id("block/bundled_cable/gray/bottom_z"),
            id("block/bundled_cable/gray/lower_side"),
            id("block/bundled_cable/gray/upper_side"),
        ),
        LIGHT_GRAY to WireIds(
            id("block/bundled_cable/light_gray/top_cross"),
            id("block/bundled_cable/light_gray/top_x"),
            id("block/bundled_cable/light_gray/top_z"),
            id("block/bundled_cable/light_gray/bottom_cross"),
            id("block/bundled_cable/light_gray/bottom_x"),
            id("block/bundled_cable/light_gray/bottom_z"),
            id("block/bundled_cable/light_gray/lower_side"),
            id("block/bundled_cable/light_gray/upper_side"),
        ),
        CYAN to WireIds(
            id("block/bundled_cable/cyan/top_cross"),
            id("block/bundled_cable/cyan/top_x"),
            id("block/bundled_cable/cyan/top_z"),
            id("block/bundled_cable/cyan/bottom_cross"),
            id("block/bundled_cable/cyan/bottom_x"),
            id("block/bundled_cable/cyan/bottom_z"),
            id("block/bundled_cable/cyan/lower_side"),
            id("block/bundled_cable/cyan/upper_side"),
        ),
        PURPLE to WireIds(
            id("block/bundled_cable/purple/top_cross"),
            id("block/bundled_cable/purple/top_x"),
            id("block/bundled_cable/purple/top_z"),
            id("block/bundled_cable/purple/bottom_cross"),
            id("block/bundled_cable/purple/bottom_x"),
            id("block/bundled_cable/purple/bottom_z"),
            id("block/bundled_cable/purple/lower_side"),
            id("block/bundled_cable/purple/upper_side"),
        ),
        BLUE to WireIds(
            id("block/bundled_cable/blue/top_cross"),
            id("block/bundled_cable/blue/top_x"),
            id("block/bundled_cable/blue/top_z"),
            id("block/bundled_cable/blue/bottom_cross"),
            id("block/bundled_cable/blue/bottom_x"),
            id("block/bundled_cable/blue/bottom_z"),
            id("block/bundled_cable/blue/lower_side"),
            id("block/bundled_cable/blue/upper_side"),
        ),
        BROWN to WireIds(
            id("block/bundled_cable/brown/top_cross"),
            id("block/bundled_cable/brown/top_x"),
            id("block/bundled_cable/brown/top_z"),
            id("block/bundled_cable/brown/bottom_cross"),
            id("block/bundled_cable/brown/bottom_x"),
            id("block/bundled_cable/brown/bottom_z"),
            id("block/bundled_cable/brown/lower_side"),
            id("block/bundled_cable/brown/upper_side"),
        ),
        GREEN to WireIds(
            id("block/bundled_cable/green/top_cross"),
            id("block/bundled_cable/green/top_x"),
            id("block/bundled_cable/green/top_z"),
            id("block/bundled_cable/green/bottom_cross"),
            id("block/bundled_cable/green/bottom_x"),
            id("block/bundled_cable/green/bottom_z"),
            id("block/bundled_cable/green/lower_side"),
            id("block/bundled_cable/green/upper_side"),
        ),
        RED to WireIds(
            id("block/bundled_cable/red/top_cross"),
            id("block/bundled_cable/red/top_x"),
            id("block/bundled_cable/red/top_z"),
            id("block/bundled_cable/red/bottom_cross"),
            id("block/bundled_cable/red/bottom_x"),
            id("block/bundled_cable/red/bottom_z"),
            id("block/bundled_cable/red/lower_side"),
            id("block/bundled_cable/red/upper_side"),
        ),
        BLACK to WireIds(
            id("block/bundled_cable/black/top_cross"),
            id("block/bundled_cable/black/top_x"),
            id("block/bundled_cable/black/top_z"),
            id("block/bundled_cable/black/bottom_cross"),
            id("block/bundled_cable/black/bottom_x"),
            id("block/bundled_cable/black/bottom_z"),
            id("block/bundled_cable/black/lower_side"),
            id("block/bundled_cable/black/upper_side"),
        )
    )
}