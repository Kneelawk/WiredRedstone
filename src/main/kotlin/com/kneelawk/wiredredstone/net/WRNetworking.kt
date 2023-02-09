package com.kneelawk.wiredredstone.net

import alexiil.mc.lib.net.NetByteBuf
import com.kneelawk.wiredredstone.WRConstants
import com.kneelawk.wiredredstone.WRConstants.id
import com.kneelawk.wiredredstone.WRConstants.message
import com.kneelawk.wiredredstone.config.CommonConfig
import com.kneelawk.wiredredstone.part.AbstractConnectablePart
import com.kneelawk.wiredredstone.screenhandler.RedstoneAssemblerScreenHandler
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking
import net.minecraft.text.Text

object WRNetworking {
    const val NETWORKING_VERSION: Int = 1

    val HELLO_CHANNEL = id("hello")
    val CONFIG_SYNC_CHANNEL = id("config_sync")

    private val MISSING_MOD_TEXT = Text.literal("Client is missing Wired Redstone mod version >= 0.4.15")

    fun init() {
        AbstractConnectablePart.initNetworking()
        RedstoneAssemblerScreenHandler.initNetworking()

        ServerLoginConnectionEvents.QUERY_START.register { _, _, sender, _ ->
            sender.sendPacket(HELLO_CHANNEL, PacketByteBufs.empty())
        }
        ServerLoginNetworking.registerGlobalReceiver(
            HELLO_CHANNEL
        ) { _, handler, understood, buf, _, sender ->
            if (!understood) {
                handler.disconnect(MISSING_MOD_TEXT)
                return@registerGlobalReceiver
            }

            @Suppress("name_shadowing")
            val buf = NetByteBuf.asNetByteBuf(buf)

            val clientNetVersion = buf.readVarInt()
            val clientModVersion = buf.readString(64)

            if (clientNetVersion != NETWORKING_VERSION) {
                handler.disconnect(
                    message("login.bad_version", clientModVersion, WRConstants.MOD_VERSION)
                )
                return@registerGlobalReceiver
            }

            val toSend = NetByteBuf.buffer()
            CommonConfig.toSyncPacket(toSend)
            sender.sendPacket(CONFIG_SYNC_CHANNEL, toSend)
        }
        ServerLoginNetworking.registerGlobalReceiver(CONFIG_SYNC_CHANNEL) { _, handler, understood, _, _, _ ->
            if (!understood) {
                handler.disconnect(MISSING_MOD_TEXT)
            }
        }
    }
}
