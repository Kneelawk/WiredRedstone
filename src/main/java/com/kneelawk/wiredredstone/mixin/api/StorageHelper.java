package com.kneelawk.wiredredstone.mixin.api;

import com.kneelawk.wiredredstone.logic.phantom.PhantomRedstoneStorage;
import net.minecraft.server.world.ServerWorld;

public class StorageHelper {
    public static PhantomRedstoneStorage getPhantomRedstone(ServerWorld world) {
        return ((WiredRedstoneChunkAccess) world.getChunkManager().threadedAnvilChunkStorage).wiredredstone_getPhantomRedstone();
    }
}
