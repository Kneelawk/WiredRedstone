package com.kneelawk.wiredredstone.net

import alexiil.mc.lib.net.NetByteBuf
import com.kneelawk.wiredredstone.WRConstants
import com.kneelawk.wiredredstone.WRConstants.id
import com.kneelawk.wiredredstone.WRLog
import com.kneelawk.wiredredstone.config.CommonConfig
import com.kneelawk.wiredredstone.config.SyncPhase
import com.kneelawk.wiredredstone.part.AbstractConnectablePart
import com.kneelawk.wiredredstone.screenhandler.RedstoneAssemblerScreenHandler
import io.netty.handler.codec.DecoderException
import net.fabricmc.fabric.api.networking.v1.*
import net.minecraft.network.PacketByteBuf
import net.minecraft.text.Text
import net.minecraft.util.thread.ThreadExecutor
import java.net.SocketAddress
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

object WRNetworking {
    const val NETWORKING_VERSION: Int = 1

    val HELLO_CHANNEL = id("hello")
    val CONFIG_SYNC_CHANNEL = id("config_sync")

    private val MISSING_MOD_LOGIN_TEXT = Text.literal("Client is missing Wired Redstone mod version >= 0.4.15")
    private val MISSING_MOD_PLAY_TEXT = Text.literal("Client is missing Wired Redstone mod version >= 0.4.16")

    private val responseCheckerExecutor = Executors.newSingleThreadScheduledExecutor()
    private val missingResponses = HashSet<SocketAddress>()

    fun init() {
        AbstractConnectablePart.initNetworking()
        RedstoneAssemblerScreenHandler.initNetworking()

        if (CommonConfig.local.syncEnabled) {
            when (CommonConfig.local.syncPhase) {
                SyncPhase.LOGIN -> {
                    WRLog.log.info("[Wired Redstone] Synchronization will happen during LOGIN phase.")
                    ServerLoginConnectionEvents.QUERY_START.register { _, _, sender, _ ->
                        startVersionCheck(sender)
                    }
                    ServerLoginNetworking.registerGlobalReceiver(
                        HELLO_CHANNEL
                    ) { _, handler, understood, buf, _, sender ->
                        handler.connectionInfo
                        checkVersionAndSync(
                            understood, handler.connection.address, handler::disconnect, buf, sender,
                            MISSING_MOD_LOGIN_TEXT
                        )
                    }
                    ServerLoginNetworking.registerGlobalReceiver(
                        CONFIG_SYNC_CHANNEL
                    ) { _, handler, understood, _, _, _ ->
                        if (!understood) {
                            handler.disconnect(MISSING_MOD_LOGIN_TEXT)
                        }
                    }
                }
                SyncPhase.PLAY -> {
                    WRLog.log.info("[Wired Redstone] Synchronization will happen during PLAY phase.")
                    ServerPlayConnectionEvents.JOIN.register { handler, sender, server ->
                        startVersionCheck(sender)
                        startResponseRequirementCountdown(handler.connection.address, server) {
                            handler.disconnect(MISSING_MOD_PLAY_TEXT)
                        }
                    }
                    ServerPlayNetworking.registerGlobalReceiver(HELLO_CHANNEL) { _, _, handler, buf, sender ->
                        checkVersionAndSync(
                            true, handler.connection.address, handler::disconnect, buf, sender, MISSING_MOD_PLAY_TEXT
                        )
                    }
                }
            }
        }

        ServerPlayConnectionEvents.DISCONNECT.register { handler, _ ->
            missingResponses.remove(handler.connection.address)
        }
        ServerLoginConnectionEvents.DISCONNECT.register { handler, _ ->
            missingResponses.remove(handler.connection.address)
        }
    }

    private fun startVersionCheck(sender: PacketSender) {
        sender.sendPacket(HELLO_CHANNEL, PacketByteBufs.empty())
    }

    private fun startResponseRequirementCountdown(
        address: SocketAddress, executor: ThreadExecutor<*>, disconnect: () -> Unit
    ) {
        missingResponses.add(address)

        val delay = CommonConfig.local.versionCheckTimeout
        responseCheckerExecutor.schedule({
            executor.execute {
                if (missingResponses.contains(address)) {
                    disconnect()
                }
            }
        }, delay, TimeUnit.MILLISECONDS)
    }

    private fun checkVersionAndSync(
        understood: Boolean, address: SocketAddress, disconnect: (Text) -> Unit, buf: PacketByteBuf?,
        sender: PacketSender, missingText: Text
    ) {
        if (!understood) {
            disconnect(missingText)
            return
        }

        try {
            @Suppress("name_shadowing") val buf = NetByteBuf.asNetByteBuf(buf)

            val clientNetVersion = buf.readVarInt()
            val clientModVersion = buf.readString(64)

            if (clientNetVersion != NETWORKING_VERSION) {
                disconnect(
                    Text.literal(
                        "Your client version of Wired Redstone ($clientModVersion) is incompatible with this server. Please install Wired Redstone version ${WRConstants.MOD_VERSION}."
                    )
                )
                return
            }

            val toSend = NetByteBuf.buffer()
            CommonConfig.toSyncPacket(toSend)
            sender.sendPacket(CONFIG_SYNC_CHANNEL, toSend)
            missingResponses.remove(address)
        } catch (e: IndexOutOfBoundsException) {
            disconnect(missingText)
        } catch (e: DecoderException) {
            disconnect(missingText)
        }
    }
}
