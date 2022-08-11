package com.kneelawk.wiredredstone.mixin.create.impl;

import alexiil.mc.lib.multipart.impl.LibMultiPart;
import alexiil.mc.lib.multipart.impl.MultipartBlockEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.StructureTransform;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * This mixin gets Create and LMP to play nice together while also allowing wires' custom rotation logic to work.
 */
@Mixin(StructureTransform.class)
public class StructureTransformMixin {
    @Inject(remap = false, method = "apply(Lnet/minecraft/block/BlockState;)Lnet/minecraft/block/BlockState;", at = @At("HEAD"), cancellable = true)
    private void onApplyBlockState(BlockState state, CallbackInfoReturnable<BlockState> cir) {
        if (state.getBlock() == LibMultiPart.BLOCK) {
            // immediately cancel here because otherwise apply will call mirror which crashes
            cir.setReturnValue(state);
        }
    }

    @Inject(remap = false, method = "apply(Lnet/minecraft/block/entity/BlockEntity;)V", at = @At("HEAD"))
    private void onApplyTileEntity(BlockEntity te, CallbackInfo ci) {
        if (te instanceof MultipartBlockEntity multi && multi.isServerWorld()) {
            // For some reason, create forgets to call cancelRemoval on BlockEntities, meaning that parts' onAdded
            // methods never get called.
            multi.cancelRemoval();

            // Transformations could be easily solved if MultipartBlockEntity were to implement ITransformableTE but
            // that would mean a hard-dependency on Create which is not an option for LMP.

            // I'm choosing not to just call MultipartBlockEntity.mirror or rotate here because: (1) those do not
            // function in a way that is usable by WR components and are thus not implemented for those components, and
            // (2) if multiple mods do this, they could cause quite a mess, all rotating each other's components
            // multiple times.
        }
    }
}
