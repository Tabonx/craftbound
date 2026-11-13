package com.craftbound.client.jei.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.craftbound.client.jei.BookRecipeRender;

// Hides newer JEI's "#" badge on slots that cycle through several items, which vanilla never
// marked. Older JEI has no badge, and there this injects nothing. The handler takes no arguments
// because the badge's parameters differ between JEI releases.
@Mixin(targets = "mezz.jei.library.gui.ingredients.RecipeSlot", remap = false)
public class CandidatesBadgeMixin
{
    @Inject(method = "drawCandidatesBadge", at = @At("HEAD"), cancellable = true)
    private void craftbound$hide(CallbackInfo ci)
    {
        if (BookRecipeRender.active())
            ci.cancel();
    }
}
