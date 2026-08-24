package com.craftbound.client.mixin.jei;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

// Stops JEI drawing anything over a screen. This is the one call that draws both of its overlays,
// so cancelling it takes the item list, the bookmark list and the two corner buttons with it.
//
// The buttons are the reason this exists at all: JEI draws its lists only where they fit, which
// JeiOverlayHider takes away, but it draws the buttons whenever the screen is valid, so no amount
// of denying it room will hide them.
@Mixin(targets = "mezz.jei.gui.events.GuiEventHandler", remap = false)
public class JeiOverlayMixin
{
    //? if >=1.21.8 {
    /*// Newer JEI draws through two entry points, and the container-screen one is what covers an
    // open inventory, which is exactly where the book lives.
    @Inject(method = "drawForScreen", at = @At("HEAD"), cancellable = true)
    private void craftbound$hideOverlays(Screen screen, GuiGraphics graphics, int mouseX, int mouseY,
            CallbackInfo ci)
    {
        ci.cancel();
    }

    @Inject(method = "drawForContainerScreen", at = @At("HEAD"), cancellable = true)
    private void craftbound$hideContainerOverlays(AbstractContainerScreen<?> screen, GuiGraphics graphics,
            int mouseX, int mouseY, CallbackInfo ci)
    {
        ci.cancel();
    }
    *///?} else {
    @Inject(method = "onDrawScreenPost", at = @At("HEAD"), cancellable = true)
    private void craftbound$hideOverlays(Screen screen, GuiGraphics graphics, int mouseX, int mouseY,
            CallbackInfo ci)
    {
        ci.cancel();
    }

    @Inject(method = "onDrawForeground", at = @At("HEAD"), cancellable = true)
    private void craftbound$hideForeground(AbstractContainerScreen<?> screen, GuiGraphics graphics,
            int mouseX, int mouseY, CallbackInfo ci)
    {
        ci.cancel();
    }
    //?}
}
