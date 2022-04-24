package com.kneelawk.wiredredstone.client.render.part

import alexiil.mc.lib.multipart.api.render.PartRenderContext
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import com.kneelawk.wiredredstone.WRConstants
import com.kneelawk.wiredredstone.client.render.*
import com.kneelawk.wiredredstone.part.key.BundledCablePartKey
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh
import net.minecraft.util.DyeColor.*

object BundledCablePartBaker : WRPartBaker<BundledCablePartKey> {
    private val cache: LoadingCache<BundledCablePartKey, Mesh> =
        CacheBuilder.newBuilder().build(CacheLoader.from(::makeMesh))

    private fun makeMesh(key: BundledCablePartKey): Mesh {
        val builder = RenderUtils.MESH_BUILDER
        val emitter = TransformingQuadEmitter.Single(builder.emitter, SideQuadTransform(key.side))

        val sprites = BUNDLED_CABLE_IDS[key.color]!!.lookup()

        WireRendering.emitWire(
            conn = key.connections,
            axis = key.side.axis,
            wireHeight = 4f,
            wireWidth = 6f,
            topCrossSprite = sprites.topCross,
            topXSprite = sprites.topX,
            topZSprite = sprites.topZ,
            sideSprite = sprites.side,
            openEndSprite = sprites.openEnd,
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
        for (wire in BUNDLED_CABLE_IDS.values) {
            wire.register(registry)
        }
    }

    private val BUNDLED_CABLE_IDS = mapOf(
        null to WireIds(
            WRConstants.id("block/bundled_cable/top_cross"),
            WRConstants.id("block/bundled_cable/top_x"),
            WRConstants.id("block/bundled_cable/top_z"),
            WRConstants.id("block/bundled_cable/side"),
            WRConstants.id("block/bundled_cable/end")
        ),
        WHITE to WireIds(
            WRConstants.id("block/bundled_cable/white_top_cross"),
            WRConstants.id("block/bundled_cable/white_top_x"),
            WRConstants.id("block/bundled_cable/white_top_z"),
            WRConstants.id("block/bundled_cable/white_side"),
            WRConstants.id("block/bundled_cable/white_end")
        ),
        ORANGE to WireIds(
            WRConstants.id("block/bundled_cable/orange_top_cross"),
            WRConstants.id("block/bundled_cable/orange_top_x"),
            WRConstants.id("block/bundled_cable/orange_top_z"),
            WRConstants.id("block/bundled_cable/orange_side"),
            WRConstants.id("block/bundled_cable/orange_end")
        ),
        MAGENTA to WireIds(
            WRConstants.id("block/bundled_cable/magenta_top_cross"),
            WRConstants.id("block/bundled_cable/magenta_top_x"),
            WRConstants.id("block/bundled_cable/magenta_top_z"),
            WRConstants.id("block/bundled_cable/magenta_side"),
            WRConstants.id("block/bundled_cable/magenta_end")
        ),
        LIGHT_BLUE to WireIds(
            WRConstants.id("block/bundled_cable/light_blue_top_cross"),
            WRConstants.id("block/bundled_cable/light_blue_top_x"),
            WRConstants.id("block/bundled_cable/light_blue_top_z"),
            WRConstants.id("block/bundled_cable/light_blue_side"),
            WRConstants.id("block/bundled_cable/light_blue_end")
        ),
        YELLOW to WireIds(
            WRConstants.id("block/bundled_cable/yellow_top_cross"),
            WRConstants.id("block/bundled_cable/yellow_top_x"),
            WRConstants.id("block/bundled_cable/yellow_top_z"),
            WRConstants.id("block/bundled_cable/yellow_side"),
            WRConstants.id("block/bundled_cable/yellow_end")
        ),
        LIME to WireIds(
            WRConstants.id("block/bundled_cable/lime_top_cross"),
            WRConstants.id("block/bundled_cable/lime_top_x"),
            WRConstants.id("block/bundled_cable/lime_top_z"),
            WRConstants.id("block/bundled_cable/lime_side"),
            WRConstants.id("block/bundled_cable/lime_end")
        ),
        PINK to WireIds(
            WRConstants.id("block/bundled_cable/pink_top_cross"),
            WRConstants.id("block/bundled_cable/pink_top_x"),
            WRConstants.id("block/bundled_cable/pink_top_z"),
            WRConstants.id("block/bundled_cable/pink_side"),
            WRConstants.id("block/bundled_cable/pink_end")
        ),
        GRAY to WireIds(
            WRConstants.id("block/bundled_cable/gray_top_cross"),
            WRConstants.id("block/bundled_cable/gray_top_x"),
            WRConstants.id("block/bundled_cable/gray_top_z"),
            WRConstants.id("block/bundled_cable/gray_side"),
            WRConstants.id("block/bundled_cable/gray_end")
        ),
        LIGHT_GRAY to WireIds(
            WRConstants.id("block/bundled_cable/light_gray_top_cross"),
            WRConstants.id("block/bundled_cable/light_gray_top_x"),
            WRConstants.id("block/bundled_cable/light_gray_top_z"),
            WRConstants.id("block/bundled_cable/light_gray_side"),
            WRConstants.id("block/bundled_cable/light_gray_end")
        ),
        CYAN to WireIds(
            WRConstants.id("block/bundled_cable/cyan_top_cross"),
            WRConstants.id("block/bundled_cable/cyan_top_x"),
            WRConstants.id("block/bundled_cable/cyan_top_z"),
            WRConstants.id("block/bundled_cable/cyan_side"),
            WRConstants.id("block/bundled_cable/cyan_end")
        ),
        PURPLE to WireIds(
            WRConstants.id("block/bundled_cable/purple_top_cross"),
            WRConstants.id("block/bundled_cable/purple_top_x"),
            WRConstants.id("block/bundled_cable/purple_top_z"),
            WRConstants.id("block/bundled_cable/purple_side"),
            WRConstants.id("block/bundled_cable/purple_end")
        ),
        BLUE to WireIds(
            WRConstants.id("block/bundled_cable/blue_top_cross"),
            WRConstants.id("block/bundled_cable/blue_top_x"),
            WRConstants.id("block/bundled_cable/blue_top_z"),
            WRConstants.id("block/bundled_cable/blue_side"),
            WRConstants.id("block/bundled_cable/blue_end")
        ),
        BROWN to WireIds(
            WRConstants.id("block/bundled_cable/brown_top_cross"),
            WRConstants.id("block/bundled_cable/brown_top_x"),
            WRConstants.id("block/bundled_cable/brown_top_z"),
            WRConstants.id("block/bundled_cable/brown_side"),
            WRConstants.id("block/bundled_cable/brown_end")
        ),
        GREEN to WireIds(
            WRConstants.id("block/bundled_cable/green_top_cross"),
            WRConstants.id("block/bundled_cable/green_top_x"),
            WRConstants.id("block/bundled_cable/green_top_z"),
            WRConstants.id("block/bundled_cable/green_side"),
            WRConstants.id("block/bundled_cable/green_end")
        ),
        RED to WireIds(
            WRConstants.id("block/bundled_cable/red_top_cross"),
            WRConstants.id("block/bundled_cable/red_top_x"),
            WRConstants.id("block/bundled_cable/red_top_z"),
            WRConstants.id("block/bundled_cable/red_side"),
            WRConstants.id("block/bundled_cable/red_end")
        ),
        BLACK to WireIds(
            WRConstants.id("block/bundled_cable/black_top_cross"),
            WRConstants.id("block/bundled_cable/black_top_x"),
            WRConstants.id("block/bundled_cable/black_top_z"),
            WRConstants.id("block/bundled_cable/black_side"),
            WRConstants.id("block/bundled_cable/black_end")
        )
    )
}