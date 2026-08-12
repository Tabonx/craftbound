package com.craftbound.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.components.toasts.RecipeToast;

// Vanilla still tracks its own recipe unlocks and toasts them, even though its recipe book is
// forced hidden (see RecipeBookVisibilityMixin). That leaves two notifications side by side — one
// telling the player to check a book they do not have — and the two disagree, since vanilla unlocks
// on its own rules rather than Craftbound's progression. Suppress it and let RecipeUnlockToast be
// the single answer to "what did I just unlock?".
@Mixin(RecipeToast.class)
public abstract class RecipeToastMixin
{
    @Inject(method = "addOrUpdate", at = @At("HEAD"), cancellable = true)
    private static void craftbound$suppress(CallbackInfo ci)
    {
        ci.cancel();
    }
}
