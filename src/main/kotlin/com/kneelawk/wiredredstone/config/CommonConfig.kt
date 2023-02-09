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
    @Comment("Configures values relating to the Redstone Assembler.")
    val assembler: AssemblerConfig = AssemblerConfig()
) : WrappedConfig() {
    companion object {
        init {
            WRLog.log.info("Wired Redstone: loading common config...")
        }

        private val ENV = ConfigEnvironment(FabricLoader.getInstance().configDir, Json5Serializer)

        private val LOADED: CommonConfig = Config.create(ENV, WRConstants.MOD_ID, "common", CommonConfig::class.java)

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

                WRLog.log.info("Wired Redstone: synced config values from server.")

                PacketByteBufs.empty()
            })
        }

        fun restoreDefault(executor: ThreadExecutor<*>) {
            executor.execute {
                WRLog.log.info("Wired Redstone: restoring local client config values.")

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
