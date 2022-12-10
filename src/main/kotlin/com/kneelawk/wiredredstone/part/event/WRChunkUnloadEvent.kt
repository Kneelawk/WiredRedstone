package com.kneelawk.wiredredstone.part.event

import alexiil.mc.lib.multipart.api.event.ContextlessEvent
import alexiil.mc.lib.multipart.api.event.MultipartEvent
import alexiil.mc.lib.multipart.impl.MultipartBlockEntity
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents

/**
 * Fired on the server when a part's chunk is unloaded for any reason.
 */
object WRChunkUnloadEvent : MultipartEvent(), ContextlessEvent {
    fun init() {
        ServerChunkEvents.CHUNK_UNLOAD.register { _, chunk ->
            for ((_, be) in chunk.blockEntities) {
                // FIXME: using LMP internal classes
                if (be is MultipartBlockEntity) {
                    be.container.eventBus.fireEvent(WRChunkUnloadEvent)
                }
            }
        }
    }
}
