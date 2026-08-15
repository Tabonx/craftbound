package com.craftbound.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import mezz.jei.library.gui.recipes.ShapelessIcon;

import net.minecraft.client.gui.GuiGraphics;

// JEI stamps a shapeless marker on the corner of shapeless crafting recipes. Vanilla's recipe book
// never said so, and the book only shows one recipe at a time, so the icon is noise rather than
// news. Silenced here rather than in the vendored source, which stays as JEI wrote it.
@Mixin(ShapelessIcon.class)
public class ShapelessIconMixin
{
    @Inject(method = "draw", at = @At("HEAD"), cancellable = true)
    private void craftbound$hide(GuiGraphics graphics, CallbackInfo ci)
    {
        ci.cancel();
    }

    // Without this the tooltip would still appear over the space the icon no longer occupies.
    @Inject(method = "isMouseOver", at = @At("HEAD"), cancellable = true)
    private void craftbound$neverHovered(int mouseX, int mouseY, CallbackInfoReturnable<Boolean> cir)
    {
        cir.setReturnValue(false);
    }
}
