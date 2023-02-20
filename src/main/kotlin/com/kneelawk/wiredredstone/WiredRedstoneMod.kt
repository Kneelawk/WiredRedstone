package com.kneelawk.wiredredstone

import com.kneelawk.wiredredstone.block.WRBlocks
import com.kneelawk.wiredredstone.blockentity.WRBlockEntities
import com.kneelawk.wiredredstone.compat.cc.CCIntegrationHandler
import com.kneelawk.wiredredstone.compat.create.CreateCompatHandler
import com.kneelawk.wiredredstone.config.CommonConfig
import com.kneelawk.wiredredstone.item.WRItems
import com.kneelawk.wiredredstone.net.WRNetworking
import com.kneelawk.wiredredstone.node.WRBlockNodeDiscoverer
import com.kneelawk.wiredredstone.node.WRBlockNodes
import com.kneelawk.wiredredstone.part.WRParts
import com.kneelawk.wiredredstone.part.event.WRChunkUnloadEvent
import com.kneelawk.wiredredstone.recipe.WRRecipes
import com.kneelawk.wiredredstone.screenhandler.WRScreenHandlers
import com.kneelawk.wiredredstone.util.RedstoneLogic
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents

@Suppress("unused")
fun init() {
    WRLog.log.info("[Wired Redstone] Initializing...")

    CommonConfig.ensureInit()
    WRParts.init()
    WRBlocks.init()
    WRItems.init()
    WRBlockEntities.init()
    WRBlockNodes.init()
    WRBlockNodeDiscoverer.init()
    WRRecipes.init()
    WRScreenHandlers.init()
    WRNetworking.init()
    WRChunkUnloadEvent.init()

    CCIntegrationHandler.init()
    CreateCompatHandler.init()

    // Not sure where better to do this
    ServerTickEvents.END_WORLD_TICK.register { world ->
        RedstoneLogic.flushUpdates(world)
    }

    WRLog.log.info("[Wired Redstone] Initialized.")
}
