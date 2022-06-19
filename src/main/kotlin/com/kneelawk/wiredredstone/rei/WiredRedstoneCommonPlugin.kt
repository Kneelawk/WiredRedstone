package com.kneelawk.wiredredstone.rei

import com.kneelawk.wiredredstone.screenhandler.RedstoneAssemblerScreenHandler
import me.shedaniel.rei.api.common.display.DisplaySerializerRegistry
import me.shedaniel.rei.api.common.plugins.REIServerPlugin
import me.shedaniel.rei.api.common.transfer.info.MenuInfoRegistry
import me.shedaniel.rei.api.common.transfer.info.simple.SimpleMenuInfoProvider
import me.shedaniel.rei.plugin.common.BuiltinPlugin

@Suppress("unused")
class WiredRedstoneCommonPlugin : REIServerPlugin {
    override fun registerDisplaySerializer(registry: DisplaySerializerRegistry) {
        registry.register(WiredRedstoneREI.REDSTONE_ASSEMBLER_CATEGORY, RedstoneAssemblerDisplay.Serializer)
    }

    override fun registerMenuInfo(registry: MenuInfoRegistry) {
        registry.register(
            WiredRedstoneREI.REDSTONE_ASSEMBLER_CATEGORY,
            RedstoneAssemblerScreenHandler::class.java,
            SimpleMenuInfoProvider.of(::RedstoneAssemblerMenuInfo)
        )

        // allow use of crafting recipes as well
        registry.register(
            BuiltinPlugin.CRAFTING,
            RedstoneAssemblerScreenHandler::class.java,
            SimpleMenuInfoProvider.of(::RedstoneAssemblerCraftingMenuInfo)
        )
    }
}
