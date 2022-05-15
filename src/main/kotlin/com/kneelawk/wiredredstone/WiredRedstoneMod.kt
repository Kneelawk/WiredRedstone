package com.kneelawk.wiredredstone

import com.kneelawk.wiredredstone.item.WRItems
import com.kneelawk.wiredredstone.node.WRBlockNodeDiscoverer
import com.kneelawk.wiredredstone.node.WRBlockNodes
import com.kneelawk.wiredredstone.part.WRParts
import com.kneelawk.wiredredstone.util.RedstoneLogic
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents

@Suppress("unused")
fun init() {
    WRParts.init()
    WRItems.init()
    WRBlockNodes.init()
    WRBlockNodeDiscoverer.init()

    // Not sure where better to do this
    ServerTickEvents.END_WORLD_TICK.register { world ->
        RedstoneLogic.flushUpdates(world)
    }
}
