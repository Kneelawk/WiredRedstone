package com.kneelawk.wiredredstone.mixin.impl;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;

@Mixin(GameRenderer.class)
public interface GameRendererAccessor {
    @Invoker("getFov")
    double wiredredstone_getFov(Camera camera, float tickDelta, boolean changingFov);

    @Invoker("bobViewWhenHurt")
    void wiredredstone_bobViewWhenHurt(MatrixStack matrices, float tickDelta);

    @Invoker("bobView")
    void wiredredstone_bobView(MatrixStack matrices, float tickDelta);

    @Accessor
    int getTicks();
}
