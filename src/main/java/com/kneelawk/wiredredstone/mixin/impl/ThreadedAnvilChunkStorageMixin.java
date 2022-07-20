package com.kneelawk.wiredredstone.mixin.impl;

import com.kneelawk.wiredredstone.WRConstants;
import com.kneelawk.wiredredstone.WRLog;
import com.kneelawk.wiredredstone.logic.phantom.PhantomRedstoneStorage;
import com.kneelawk.wiredredstone.mixin.api.WiredRedstoneChunkAccess;
import com.mojang.datafixers.DataFixer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.thread.ThreadExecutor;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.chunk.ChunkProvider;
import net.minecraft.world.chunk.ChunkStatusChangeListener;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.Executor;
import java.util.function.Supplier;

/**
 * Allows storage of phantom redstone references.
 */
@Mixin(ThreadedAnvilChunkStorage.class)
public class ThreadedAnvilChunkStorageMixin implements WiredRedstoneChunkAccess {
    @Shadow
    @Final
    ServerWorld world;

    @Unique
    private PhantomRedstoneStorage phantomRedstoneStorage;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onCreate(ServerWorld world, LevelStorage.Session session, DataFixer dataFixer,
                          StructureTemplateManager structureTemplateManager, Executor executor,
                          ThreadExecutor<Runnable> mainThreadExecutor, ChunkProvider chunkProvider,
                          ChunkGenerator chunkGenerator,
                          WorldGenerationProgressListener worldGenerationProgressListener,
                          ChunkStatusChangeListener chunkStatusChangeListener,
                          Supplier<PersistentStateManager> persistentStateManagerFactory, int viewDistance,
                          boolean dsync, CallbackInfo ci) {
        phantomRedstoneStorage = new PhantomRedstoneStorage(world,
                session.getWorldDirectory(world.getRegistryKey()).resolve(WRConstants.DATA_DIRNAME)
                        .resolve(WRConstants.MOD_ID).resolve(WRConstants.PHANTOM_REDSTONE_DIRNAME), dsync);
    }

    @Inject(method = "save(Z)V", at = @At("HEAD"))
    private void onSave(boolean flush, CallbackInfo ci) {
        try {
            phantomRedstoneStorage.saveAll();
        } catch (Exception e) {
            WRLog.error("Error saving Phantom Redstone storage. World '{}'/{}", world,
                    world.getRegistryKey().getValue(), e);
        }
    }

    @Override
    public PhantomRedstoneStorage wiredredstone_getPhantomRedstone() {
        return phantomRedstoneStorage;
    }
}
