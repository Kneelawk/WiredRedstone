package com.kneelawk.wiredredstone.mixin.impl;

import alexiil.mc.lib.multipart.impl.MultipartBlockEntity;
import alexiil.mc.lib.multipart.impl.PartHolder;
import com.kneelawk.wiredredstone.part.BlockEntityRemoveListener;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(WorldChunk.class)
public abstract class WorldChunkMixin {
    @Inject(method = "removeBlockEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/BlockEntity;markRemoved()V"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void onRemoveBlockEntity(BlockPos pos, CallbackInfo ci, BlockEntity blockEntity) {
        if (!getWorld().isClient && blockEntity instanceof MultipartBlockEntity multipart) {
            for (PartHolder holder : multipart.getContainer().parts) {
                if (holder.part instanceof BlockEntityRemoveListener listener) {
                    listener.onBlockEntityRemoved();
                }
            }
        }
    }

    @Shadow public abstract World getWorld();
}
