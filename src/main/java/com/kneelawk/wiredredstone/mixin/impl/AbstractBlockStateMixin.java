package com.kneelawk.wiredredstone.mixin.impl;

import com.kneelawk.wiredredstone.logic.phantom.PhantomRedstone;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.block.AbstractBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Allows PhantomRedstone to augment normal blocks' redstone outputs.
 */
@Mixin(AbstractBlock.AbstractBlockState.class)
public abstract class AbstractBlockStateMixin {
    @SuppressWarnings("unused")
    @ModifyReturnValue(method = "getStrongRedstonePower", at = @At("RETURN"))
    private int onGetStrongRedstonePower(int original, BlockView world, BlockPos pos, Direction oppositeFace) {
        return Math.max(original, PhantomRedstone.getStrongRedstonePower(original, world, pos, oppositeFace));
    }

    @SuppressWarnings("unused")
    @ModifyReturnValue(method = "getWeakRedstonePower", at = @At("RETURN"))
    private int onGetWeakRedstonePower(int original, BlockView world, BlockPos pos, Direction oppositeFace) {
        return Math.max(original, PhantomRedstone.getWeakRedstonePower(original, world, pos, oppositeFace));
    }
}
