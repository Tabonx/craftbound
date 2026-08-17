package com.craftbound.client.mixin.jei;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.gui.screens.Screen;

// A button that is no longer drawn still sits where it was as far as JEI's input is concerned, so
// a click in either bottom corner would toggle something the player cannot see. JEI's mouse
// handling is turned away to keep those corners inert.
//
// Keyboard input is left alone: the show-recipe and show-uses keys are handled by
// JeiScreenBlocker, on a vanilla event rather than on JEI's internals.
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
