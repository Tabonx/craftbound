package com.craftbound.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;

// Craftbound replaces the vanilla recipe book with its own panel, so force the vanilla book to
// always report itself hidden. That makes it inert: the screen never renders it, and its input
// handlers (which all gate on isVisible) fall through to the underlying screen.
@Mixin(RecipeBookComponent.class)
public abstract class RecipeBookVisibilityMixin
{
    @Inject(method = "isVisible", at = @At("HEAD"), cancellable = true)
    private void craftbound$forceHidden(CallbackInfoReturnable<Boolean> cir)
    {
        cir.setReturnValue(false);
    }
}
