package com.kneelawk.wiredredstone.mixin.impl;

import com.kneelawk.wiredredstone.WRConstants;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * This mixes into the {@code Identifier.split} logic to detect if the identifier is attempting to parse a namespaced
 * shader identifier for a Wired Redstone shader. If so, this returns the correct identifier namespace and path.
 */
@Mixin(Identifier.class)
public class IdentifierMixin {
    @Inject(method = "split", at = @At("HEAD"), cancellable = true)
    private static void shaderSplit(String id, char delimiter, CallbackInfoReturnable<String[]> cir) {
        String prefix = WRConstants.SHADER_CHECK_PREFIX;
        if (id.startsWith(prefix) && delimiter == ':') {
            String path = id.substring(prefix.length());
            cir.setReturnValue(new String[]{WRConstants.MOD_ID, WRConstants.SHADER_PREFIX + path});
        }
    }
}
