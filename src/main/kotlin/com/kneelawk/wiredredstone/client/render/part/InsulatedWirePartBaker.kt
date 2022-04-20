package com.kneelawk.wiredredstone.client.render.part

import alexiil.mc.lib.multipart.api.render.PartRenderContext
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import com.kneelawk.wiredredstone.WRConstants.id
import com.kneelawk.wiredredstone.client.render.*
import com.kneelawk.wiredredstone.part.key.InsulatedWirePartKey
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh
import net.minecraft.util.DyeColor.*

object InsulatedWirePartBaker : WRPartBaker<InsulatedWirePartKey> {
    private val cache: LoadingCache<InsulatedWirePartKey, Mesh> =
        CacheBuilder.newBuilder().build(CacheLoader.from(::makeMesh))

    private fun makeMesh(key: InsulatedWirePartKey): Mesh {
        val builder = RenderUtils.MESH_BUILDER
        val emitter = TransformingQuadEmitter.Single(builder.emitter, SideQuadTransform(key.side))

        val sprites = INSULATED_WIRE_IDS[key.color]!!.lookup()

        RenderUtils.emitWire(
            conn = key.connections,
            axis = key.side.axis,
            wireHeight = 3f,
            wireWidth = 4f,
            topCrossSprite = sprites.topCross,
            topXSprite = sprites.topX,
            topZSprite = sprites.topZ,
            sideSprite = sprites.side,
            openEndSprite = sprites.openEnd,
            sideV = 6f / 16f,
            material = WRMaterials.UNPOWERED_MATERIAL,
            emitter = emitter
        )

        return builder.build()
    }

    override fun emitQuads(key: InsulatedWirePartKey, ctx: PartRenderContext) {
        ctx.meshConsumer().accept(cache[key])
    }

    override fun registerSprites(registry: ClientSpriteRegistryCallback.Registry) {
        for (wire in INSULATED_WIRE_IDS.values) {
            wire.register(registry)
        }
    }

    private val INSULATED_WIRE_IDS = mapOf(
        WHITE to WireIds(
            id("block/white_insulated_wire_top_cross"),
            id("block/white_insulated_wire_top_x"),
            id("block/white_insulated_wire_top_z"),
            id("block/white_insulated_wire_side"),
            id("block/white_insulated_wire_end")
        ),
        ORANGE to WireIds(
            id("block/orange_insulated_wire_top_cross"),
            id("block/orange_insulated_wire_top_x"),
            id("block/orange_insulated_wire_top_z"),
            id("block/orange_insulated_wire_side"),
            id("block/orange_insulated_wire_end")
        ),
        MAGENTA to WireIds(
            id("block/magenta_insulated_wire_top_cross"),
            id("block/magenta_insulated_wire_top_x"),
            id("block/magenta_insulated_wire_top_z"),
            id("block/magenta_insulated_wire_side"),
            id("block/magenta_insulated_wire_end")
        ),
        LIGHT_BLUE to WireIds(
            id("block/light_blue_insulated_wire_top_cross"),
            id("block/light_blue_insulated_wire_top_x"),
            id("block/light_blue_insulated_wire_top_z"),
            id("block/light_blue_insulated_wire_side"),
            id("block/light_blue_insulated_wire_end")
        ),
        YELLOW to WireIds(
            id("block/yellow_insulated_wire_top_cross"),
            id("block/yellow_insulated_wire_top_x"),
            id("block/yellow_insulated_wire_top_z"),
            id("block/yellow_insulated_wire_side"),
            id("block/yellow_insulated_wire_end")
        ),
        LIME to WireIds(
            id("block/lime_insulated_wire_top_cross"),
            id("block/lime_insulated_wire_top_x"),
            id("block/lime_insulated_wire_top_z"),
            id("block/lime_insulated_wire_side"),
            id("block/lime_insulated_wire_end")
        ),
        PINK to WireIds(
            id("block/pink_insulated_wire_top_cross"),
            id("block/pink_insulated_wire_top_x"),
            id("block/pink_insulated_wire_top_z"),
            id("block/pink_insulated_wire_side"),
            id("block/pink_insulated_wire_end")
        ),
        GRAY to WireIds(
            id("block/gray_insulated_wire_top_cross"),
            id("block/gray_insulated_wire_top_x"),
            id("block/gray_insulated_wire_top_z"),
            id("block/gray_insulated_wire_side"),
            id("block/gray_insulated_wire_end")
        ),
        LIGHT_GRAY to WireIds(
            id("block/light_gray_insulated_wire_top_cross"),
            id("block/light_gray_insulated_wire_top_x"),
            id("block/light_gray_insulated_wire_top_z"),
            id("block/light_gray_insulated_wire_side"),
            id("block/light_gray_insulated_wire_end")
        ),
        CYAN to WireIds(
            id("block/cyan_insulated_wire_top_cross"),
            id("block/cyan_insulated_wire_top_x"),
            id("block/cyan_insulated_wire_top_z"),
            id("block/cyan_insulated_wire_side"),
            id("block/cyan_insulated_wire_end")
        ),
        PURPLE to WireIds(
            id("block/purple_insulated_wire_top_cross"),
            id("block/purple_insulated_wire_top_x"),
            id("block/purple_insulated_wire_top_z"),
            id("block/purple_insulated_wire_side"),
            id("block/purple_insulated_wire_end")
        ),
        BLUE to WireIds(
            id("block/blue_insulated_wire_top_cross"),
            id("block/blue_insulated_wire_top_x"),
            id("block/blue_insulated_wire_top_z"),
            id("block/blue_insulated_wire_side"),
            id("block/blue_insulated_wire_end")
        ),
        BROWN to WireIds(
            id("block/brown_insulated_wire_top_cross"),
            id("block/brown_insulated_wire_top_x"),
            id("block/brown_insulated_wire_top_z"),
            id("block/brown_insulated_wire_side"),
            id("block/brown_insulated_wire_end")
        ),
        GREEN to WireIds(
            id("block/green_insulated_wire_top_cross"),
            id("block/green_insulated_wire_top_x"),
            id("block/green_insulated_wire_top_z"),
            id("block/green_insulated_wire_side"),
            id("block/green_insulated_wire_end")
        ),
        RED to WireIds(
            id("block/red_insulated_wire_top_cross"),
            id("block/red_insulated_wire_top_x"),
            id("block/red_insulated_wire_top_z"),
            id("block/red_insulated_wire_side"),
            id("block/red_insulated_wire_end")
        ),
        BLACK to WireIds(
            id("block/black_insulated_wire_top_cross"),
            id("block/black_insulated_wire_top_x"),
            id("block/black_insulated_wire_top_z"),
            id("block/black_insulated_wire_side"),
            id("block/black_insulated_wire_end")
        )
    )
}
