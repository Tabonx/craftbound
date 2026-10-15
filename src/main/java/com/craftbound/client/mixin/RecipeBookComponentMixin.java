package com.craftbound.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.craftbound.client.RecipeBookScreenTweaks;

import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;

@Mixin(RecipeBookComponent.class)
public abstract class RecipeBookComponentMixin
{
    // Craftbound replaces the vanilla recipe book with its own panel, so force the vanilla book to
    // always report itself hidden. That makes it inert: the screen never renders it, and its input
    // handlers (which all gate on isVisible) fall through to the underlying screen.
    @Inject(method = "isVisible", at = @At("HEAD"), cancellable = true)
    private void craftbound$forceHidden(CallbackInfoReturnable<Boolean> cir)
    {
        cir.setReturnValue(false);
    }

    // A recipe-book screen asks its book where to stand while it lays itself out, so every widget it
    // and other mods place after that already follows Craftbound's book.
    @Inject(method = "updateScreenPosition", at = @At("HEAD"), cancellable = true)
    private void craftbound$placeScreen(int width, int imageWidth, CallbackInfoReturnable<Integer> cir)
    {
        cir.setReturnValue(RecipeBookScreenTweaks.hostedLeftPos(width, imageWidth));
    }
}
