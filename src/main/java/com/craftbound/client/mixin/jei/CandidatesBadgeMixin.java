package com.craftbound.client.mixin.jei;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.craftbound.client.jei.BookRecipeRender;

import net.minecraft.client.gui.GuiGraphics;

// Newer JEI stamps a "#" badge on the corner of a slot that accepts a tag or cycles through
// several items. Vanilla's recipe book never marked them, so the book leaves it out. Older JEI
// has no badge, and there this injects nothing.
@Mixin(targets = "mezz.jei.library.gui.ingredients.RecipeSlot", remap = false)
public class CandidatesBadgeMixin
{
    @Inject(method = "drawCandidatesBadge", at = @At("HEAD"), cancellable = true)
    private void craftbound$hide(GuiGraphics graphics, CallbackInfo ci)
    {
        if (BookRecipeRender.active())
            ci.cancel();
    }
}
