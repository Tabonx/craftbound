package com.craftbound.client.mixin.jei;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.craftbound.client.jei.BookRecipeRender;

import net.minecraft.client.gui.GuiGraphics;

// JEI stamps a shapeless marker on the corner of shapeless crafting recipes. Vanilla's recipe book
// never said so, and the book only shows one recipe at a time, so the icon is noise rather than
// news. Only inside the book, though: JEI's own screens keep it.
//
// The class is named as a string because Craftbound compiles against JEI's api artifacts alone,
// which is what keeps every other use of JEI on the stable surface.
@Mixin(targets = "mezz.jei.library.gui.recipes.ShapelessIcon", remap = false)
public class ShapelessIconMixin
{
    @Inject(method = "draw", at = @At("HEAD"), cancellable = true)
    private void craftbound$hide(GuiGraphics graphics, CallbackInfo ci)
    {
        if (BookRecipeRender.active())
            ci.cancel();
    }

    // Without this the tooltip would still appear over the space the icon no longer occupies.
    @Inject(method = "isMouseOver", at = @At("HEAD"), cancellable = true)
    private void craftbound$neverHovered(int mouseX, int mouseY, CallbackInfoReturnable<Boolean> cir)
    {
        if (BookRecipeRender.active())
            cir.setReturnValue(false);
    }
}
