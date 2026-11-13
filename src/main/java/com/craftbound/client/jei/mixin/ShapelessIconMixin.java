package com.craftbound.client.jei.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.craftbound.client.jei.BookRecipeRender;

import net.minecraft.client.gui.GuiGraphics;

// Hides JEI's shapeless marker inside the book only, since vanilla's book never showed one. The
// class is named as a string because Craftbound compiles against JEI's api artifacts alone.
@Mixin(targets = "mezz.jei.library.gui.recipes.ShapelessIcon", remap = false)
public class ShapelessIconMixin
{
    @Inject(method = "draw", at = @At("HEAD"), cancellable = true)
    private void craftbound$hide(GuiGraphics graphics, CallbackInfo ci)
    {
        if (BookRecipeRender.active())
            ci.cancel();
    }

    // Otherwise the tooltip would still appear where the icon was.
    @Inject(method = "isMouseOver", at = @At("HEAD"), cancellable = true)
    private void craftbound$neverHovered(int mouseX, int mouseY, CallbackInfoReturnable<Boolean> cir)
    {
        if (BookRecipeRender.active())
            cir.setReturnValue(false);
    }
}
