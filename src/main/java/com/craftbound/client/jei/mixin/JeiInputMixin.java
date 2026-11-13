package com.craftbound.client.jei.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.gui.screens.Screen;

// JEI's hidden corner buttons still take clicks, so its mouse handling is turned away. Keyboard
// input is left to JeiScreenBlocker, on a vanilla event.
@Mixin(targets = "mezz.jei.gui.input.ClientInputHandler", remap = false)
public class JeiInputMixin
{
    @Inject(method = "onGuiMouseClicked", at = @At("HEAD"), cancellable = true)
    private void craftbound$ignoreClick(Screen screen, @Coerce Object input, CallbackInfoReturnable<Boolean> cir)
    {
        cir.setReturnValue(false);
    }

    @Inject(method = "onGuiMouseReleased", at = @At("HEAD"), cancellable = true)
    private void craftbound$ignoreRelease(Screen screen, @Coerce Object input, CallbackInfoReturnable<Boolean> cir)
    {
        cir.setReturnValue(false);
    }
}
