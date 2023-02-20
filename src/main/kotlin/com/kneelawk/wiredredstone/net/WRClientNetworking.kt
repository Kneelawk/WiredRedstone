package com.kneelawk.wiredredstone.net

import alexiil.mc.lib.net.NetByteBuf
import com.kneelawk.wiredredstone.WRConstants
import com.kneelawk.wiredredstone.config.CommonConfig
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginConnectionEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.network.PacketByteBuf
import java.util.concurrent.CompletableFuture

object WRClientNetworking {
    fun init() {
        // for LOGIN phase networking
        ClientLoginNetworking.registerGlobalReceiver(WRNetworking.HELLO_CHANNEL) { _, _, _, _ ->
            CompletableFuture.completedFuture(versionCheckReply())
        }
        ClientLoginNetworking.registerGlobalReceiver(WRNetworking.CONFIG_SYNC_CHANNEL) { client, _, buf, _ ->
            CommonConfig.applySyncPacket(client, NetByteBuf.asNetByteBuf(buf))
        }

        // for PLAY phase networking
        ClientPlayNetworking.registerGlobalReceiver(WRNetworking.HELLO_CHANNEL) { _, _, _, sender ->
            sender.sendPacket(WRNetworking.HELLO_CHANNEL, versionCheckReply())
        }
        ClientPlayNetworking.registerGlobalReceiver(WRNetworking.CONFIG_SYNC_CHANNEL) { client, _, buf, _ ->
            CommonConfig.applySyncPacket(client, NetByteBuf.asNetByteBuf(buf))
        }

        // reset configs on logout
        ClientLoginConnectionEvents.DISCONNECT.register { _, client ->
            CommonConfig.restoreDefault(client)
        }
        ClientPlayConnectionEvents.DISCONNECT.register { _, client ->
            CommonConfig.restoreDefault(client)
        }
    }

    private fun versionCheckReply(): PacketByteBuf {
        val buf = NetByteBuf.buffer()
        buf.writeVarInt(WRNetworking.NETWORKING_VERSION)
        buf.writeString(WRConstants.MOD_VERSION, 64)
        return buf
    }
}
