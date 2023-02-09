package com.kneelawk.wiredredstone.net

import alexiil.mc.lib.net.NetByteBuf
import com.kneelawk.wiredredstone.WRConstants
import com.kneelawk.wiredredstone.config.CommonConfig
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginConnectionEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import java.util.concurrent.CompletableFuture

object WRClientNetworking {
    fun init() {
        ClientLoginNetworking.registerGlobalReceiver(WRNetworking.HELLO_CHANNEL) { _, _, _, _ ->
            val buf = NetByteBuf.buffer()
            buf.writeVarInt(WRNetworking.NETWORKING_VERSION)
            buf.writeString(WRConstants.MOD_VERSION, 64)
            CompletableFuture.completedFuture(buf)
        }
        ClientLoginNetworking.registerGlobalReceiver(WRNetworking.CONFIG_SYNC_CHANNEL) { client, _, buf, _ ->
            CommonConfig.applySyncPacket(client, NetByteBuf.asNetByteBuf(buf))
        }
        ClientLoginConnectionEvents.DISCONNECT.register { _, client ->
            CommonConfig.restoreDefault(client)
        }
        ClientPlayConnectionEvents.DISCONNECT.register { _, client ->
            CommonConfig.restoreDefault(client)
        }
    }
}
