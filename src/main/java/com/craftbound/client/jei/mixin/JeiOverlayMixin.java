package com.craftbound.client.jei.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

// JeiOverlayHider hides JEI's lists, but JEI draws its corner buttons whenever the screen is valid,
// so drawing is stopped here. JEI renames these entry points within a single Minecraft version, so
// every known name is listed rather than picked per version. A name the installed JEI lacks is
// skipped.
@Mixin(targets = "mezz.jei.gui.events.GuiEventHandler", remap = false)
public class JeiOverlayMixin
{
    @Inject(method = {"onDrawScreenPost", "drawForScreen", "drawForScreenForeground"},
            at = @At("HEAD"), cancellable = true)
    private void craftbound$hideOverlays(Screen screen, GuiGraphics graphics, int mouseX, int mouseY,
            CallbackInfo ci)
    {
        ci.cancel();
    }

    @Inject(method = {"onDrawForeground", "drawForContainerScreen"}, at = @At("HEAD"), cancellable = true)
    private void craftbound$hideContainerOverlays(AbstractContainerScreen<?> screen, GuiGraphics graphics,
            int mouseX, int mouseY, CallbackInfo ci)
    {
        ci.cancel();
    }

    @Inject(method = "drawForScreenBackground", at = @At("HEAD"), cancellable = true)
    private void craftbound$hideBackgrounds(Screen screen, GuiGraphics graphics, CallbackInfo ci)
    {
        ci.cancel();
    }
}
