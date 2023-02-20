package com.kneelawk.wiredredstone.config

import alexiil.mc.lib.net.NetByteBuf
import com.kneelawk.wiredredstone.WRConstants
import com.kneelawk.wiredredstone.WRLog
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.thread.ThreadExecutor
import org.quiltmc.config.api.Config
import org.quiltmc.config.api.ConfigEnvironment
import org.quiltmc.config.api.WrappedConfig
import org.quiltmc.config.api.annotations.Comment
import org.quiltmc.config.api.annotations.Processor
import java.util.concurrent.CompletableFuture
import java.util.function.Supplier

@Processor("process")
data class CommonConfig(
    @Comment("Configures when version checking and config syncing will occur.")
    @Comment("Some proxies like Velocity can't handle LOGIN phase networking,")
    @Comment("so all synchronization with a proxy must happen during the PLAY phase.")
    @Comment("However, this means that there will be a period of time that the player will be logged in")
    @Comment("before their configs have synced and with potentially an incompatible version of the mod.")
    @Comment("First, try things with LOGIN phase and if it doesn't work, try PLAY phase.")
    val syncPhase: SyncPhase = SyncPhase.LOGIN,

    @Comment("Whether version checking and syncing are enabled at all.")
    val syncEnabled: Boolean = true,

    @Comment("The amount of time in milliseconds to wait for the player to respond with a version check response.")
    @Comment("This is only used if version-checking is happening during the PLAY phase.")
    val versionCheckTimeout: Long = 20000,

    @Comment("Configures values relating to the Redstone Assembler.")
    @Comment("Some of these values will be synced to the client.")
    val assembler: AssemblerConfig = AssemblerConfig()
) : WrappedConfig() {
    companion object {
        init {
            WRLog.log.info("[Wired Redstone] Loading common config...")
        }

        private val ENV = ConfigEnvironment(FabricLoader.getInstance().configDir, Json5Serializer)

        private val LOADED: CommonConfig = Config.create(ENV, WRConstants.MOD_ID, "common", CommonConfig::class.java)

        var local = LOADED
            private set

        var current = LOADED
            private set

        fun ensureInit() {
        }

        fun toSyncPacket(packet: NetByteBuf) {
            with(LOADED) {
                packet.writeVarLong(assembler.energyCapacity)
            }
        }

        fun applySyncPacket(executor: ThreadExecutor<*>, packet: NetByteBuf): CompletableFuture<PacketByteBuf?> {
            val energyCapacity = packet.readVarLong()

            val config = LOADED.copy(assembler = LOADED.assembler.copy(energyCapacity = energyCapacity))

            return executor.submit(Supplier {
                current = config

                WRLog.log.info("[Wired Redstone] Synced config values from server.")

                PacketByteBufs.empty()
            })
        }

        fun restoreDefault(executor: ThreadExecutor<*>) {
            executor.execute {
                WRLog.log.info("[Wired Redstone] Restoring local client config values.")

                current = LOADED
            }
        }
    }

    @Suppress("unused")
    fun process(builder: Config.Builder) {
        builder.metadata(Comment.TYPE) {
            it.add(
                "Wired Redstone common config.\nSome values in here are synced to clients that join this server."
            )
        }
    }
}
