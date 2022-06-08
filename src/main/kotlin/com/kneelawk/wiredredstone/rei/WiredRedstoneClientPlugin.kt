package com.kneelawk.wiredredstone.rei

import com.kneelawk.wiredredstone.block.WRBlocks
import com.kneelawk.wiredredstone.client.screen.RedstoneAssemblerScreen
import com.kneelawk.wiredredstone.recipe.RedstoneAssemblerRecipe
import me.shedaniel.math.Rectangle
import me.shedaniel.rei.api.client.plugins.REIClientPlugin
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry
import me.shedaniel.rei.api.common.util.EntryStacks
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment

@Suppress("unused")
@Environment(EnvType.CLIENT)
class WiredRedstoneClientPlugin : REIClientPlugin {
    override fun registerCategories(registry: CategoryRegistry) {
        registry.add(RedstoneAssemblerCategory)

        registry.addWorkstations(
            WiredRedstoneREI.REDSTONE_ASSEMBLER_CATEGORY, EntryStacks.of(WRBlocks.REDSTONE_ASSEMBLER)
        )
    }

    override fun registerDisplays(registry: DisplayRegistry) {
        registry.registerFiller(RedstoneAssemblerRecipe::class.java, RedstoneAssemblerDisplay::of)
    }

    override fun registerScreens(registry: ScreenRegistry) {
        registry.registerClickArea({ screen ->
            with(screen) {
                val x = (width - RedstoneAssemblerScreen.BACKGROUND_WIDTH) / 2
                val y = (height - RedstoneAssemblerScreen.BACKGROUND_HEIGHT) / 2

                Rectangle(x + 95, y + 35, 22, 14)
            }
        }, RedstoneAssemblerScreen::class.java, WiredRedstoneREI.REDSTONE_ASSEMBLER_CATEGORY)
    }
}
