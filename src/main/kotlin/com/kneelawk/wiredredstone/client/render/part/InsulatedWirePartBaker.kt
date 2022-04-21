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

        val endSpriteId = if (key.powered) INSULATED_WIRE_END_POWERED_ID else null
        val endSprite = endSpriteId?.let(RenderUtils::getBlockSprite)
        val endMaterial = if (key.powered) WRMaterials.POWERED_MATERIAL else null

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
            openEndDecal = endSprite,
            sideV = 6f / 16f,
            material = WRMaterials.UNPOWERED_MATERIAL,
            decalMaterial = endMaterial,
            emitter = emitter
        )

        return builder.build()
    }

    override fun emitQuads(key: InsulatedWirePartKey, ctx: PartRenderContext) {
        ctx.meshConsumer().accept(cache[key])
    }

    override fun registerSprites(registry: ClientSpriteRegistryCallback.Registry) {
        registry.register(INSULATED_WIRE_END_POWERED_ID)
        for (wire in INSULATED_WIRE_IDS.values) {
            wire.register(registry)
        }
    }

    private val INSULATED_WIRE_END_POWERED_ID = id("block/insulated_wire/end_powered")
    private val INSULATED_WIRE_IDS = mapOf(
        WHITE to WireIds(
            id("block/insulated_wire/white_top_cross"),
            id("block/insulated_wire/white_top_x"),
            id("block/insulated_wire/white_top_z"),
            id("block/insulated_wire/white_side"),
            id("block/insulated_wire/white_end")
        ),
        ORANGE to WireIds(
            id("block/insulated_wire/orange_top_cross"),
            id("block/insulated_wire/orange_top_x"),
            id("block/insulated_wire/orange_top_z"),
            id("block/insulated_wire/orange_side"),
            id("block/insulated_wire/orange_end")
        ),
        MAGENTA to WireIds(
            id("block/insulated_wire/magenta_top_cross"),
            id("block/insulated_wire/magenta_top_x"),
            id("block/insulated_wire/magenta_top_z"),
            id("block/insulated_wire/magenta_side"),
            id("block/insulated_wire/magenta_end")
        ),
        LIGHT_BLUE to WireIds(
            id("block/insulated_wire/light_blue_top_cross"),
            id("block/insulated_wire/light_blue_top_x"),
            id("block/insulated_wire/light_blue_top_z"),
            id("block/insulated_wire/light_blue_side"),
            id("block/insulated_wire/light_blue_end")
        ),
        YELLOW to WireIds(
            id("block/insulated_wire/yellow_top_cross"),
            id("block/insulated_wire/yellow_top_x"),
            id("block/insulated_wire/yellow_top_z"),
            id("block/insulated_wire/yellow_side"),
            id("block/insulated_wire/yellow_end")
        ),
        LIME to WireIds(
            id("block/insulated_wire/lime_top_cross"),
            id("block/insulated_wire/lime_top_x"),
            id("block/insulated_wire/lime_top_z"),
            id("block/insulated_wire/lime_side"),
            id("block/insulated_wire/lime_end")
        ),
        PINK to WireIds(
            id("block/insulated_wire/pink_top_cross"),
            id("block/insulated_wire/pink_top_x"),
            id("block/insulated_wire/pink_top_z"),
            id("block/insulated_wire/pink_side"),
            id("block/insulated_wire/pink_end")
        ),
        GRAY to WireIds(
            id("block/insulated_wire/gray_top_cross"),
            id("block/insulated_wire/gray_top_x"),
            id("block/insulated_wire/gray_top_z"),
            id("block/insulated_wire/gray_side"),
            id("block/insulated_wire/gray_end")
        ),
        LIGHT_GRAY to WireIds(
            id("block/insulated_wire/light_gray_top_cross"),
            id("block/insulated_wire/light_gray_top_x"),
            id("block/insulated_wire/light_gray_top_z"),
            id("block/insulated_wire/light_gray_side"),
            id("block/insulated_wire/light_gray_end")
        ),
        CYAN to WireIds(
            id("block/insulated_wire/cyan_top_cross"),
            id("block/insulated_wire/cyan_top_x"),
            id("block/insulated_wire/cyan_top_z"),
            id("block/insulated_wire/cyan_side"),
            id("block/insulated_wire/cyan_end")
        ),
        PURPLE to WireIds(
            id("block/insulated_wire/purple_top_cross"),
            id("block/insulated_wire/purple_top_x"),
            id("block/insulated_wire/purple_top_z"),
            id("block/insulated_wire/purple_side"),
            id("block/insulated_wire/purple_end")
        ),
        BLUE to WireIds(
            id("block/insulated_wire/blue_top_cross"),
            id("block/insulated_wire/blue_top_x"),
            id("block/insulated_wire/blue_top_z"),
            id("block/insulated_wire/blue_side"),
            id("block/insulated_wire/blue_end")
        ),
        BROWN to WireIds(
            id("block/insulated_wire/brown_top_cross"),
            id("block/insulated_wire/brown_top_x"),
            id("block/insulated_wire/brown_top_z"),
            id("block/insulated_wire/brown_side"),
            id("block/insulated_wire/brown_end")
        ),
        GREEN to WireIds(
            id("block/insulated_wire/green_top_cross"),
            id("block/insulated_wire/green_top_x"),
            id("block/insulated_wire/green_top_z"),
            id("block/insulated_wire/green_side"),
            id("block/insulated_wire/green_end")
        ),
        RED to WireIds(
            id("block/insulated_wire/red_top_cross"),
            id("block/insulated_wire/red_top_x"),
            id("block/insulated_wire/red_top_z"),
            id("block/insulated_wire/red_side"),
            id("block/insulated_wire/red_end")
        ),
        BLACK to WireIds(
            id("block/insulated_wire/black_top_cross"),
            id("block/insulated_wire/black_top_x"),
            id("block/insulated_wire/black_top_z"),
            id("block/insulated_wire/black_side"),
            id("block/insulated_wire/black_end")
        )
    )
}
