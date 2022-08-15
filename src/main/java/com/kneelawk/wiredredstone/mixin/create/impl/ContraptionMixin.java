package com.kneelawk.wiredredstone.mixin.create.impl;

import alexiil.mc.lib.multipart.api.MultipartContainer;
import alexiil.mc.lib.multipart.api.MultipartUtil;
import com.kneelawk.wiredredstone.part.AbstractSidedPart;
import com.kneelawk.wiredredstone.part.SidedPart;
import com.kneelawk.wiredredstone.part.WRPart;
import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.structure.Structure;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.List;

/**
 * Do some extra things to multiparts when contraptions are moved.
 */
@Mixin(value = Contraption.class, remap = false)
public class ContraptionMixin {
//    @WrapWithCondition(method = "removeBlocksFromWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;removeBlockEntity(Lnet/minecraft/util/math/BlockPos;)V"))
//    private boolean shouldRemoveBlockEntity(World world, BlockPos add) {
//        MultipartContainer multipart = MultipartUtil.get(world, add);
//        if (multipart != null && multipart.getFirstPart(SidedPart.class) != null) {
//            // TODO: doesn't work, need to investigate why connections aren't getting removed when moved
//            System.out.println("Cancelling multipart removal");
//            return false;
//        } else {
//            return true;
//        }
//    }

    @Inject(method = "removeBlocksFromWorld", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/World;removeBlockEntity(Lnet/minecraft/util/math/BlockPos;)V"),
            locals = LocalCapture.CAPTURE_FAILSOFT)
    private void onRemoveBlockEntity(World world, BlockPos offset, CallbackInfo ci, List<BlockBox> minimisedGlue,
                                     boolean[] var4, int var5, int var6, boolean brittles,
                                     Iterator<Structure.StructureBlockInfo> iterator,
                                     Structure.StructureBlockInfo block, BlockPos add) {
        MultipartContainer multipart = MultipartUtil.get(world, add);
        if (multipart != null) {
            for (WRPart part : multipart.getParts(WRPart.class)) {
                // manually invoke onRemoved methods so things clean up properly
                part.onRemoved();
            }
        }
    }
}
