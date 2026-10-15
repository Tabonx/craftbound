package com.craftbound.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.craftbound.client.RecipeBookScreenTweaks;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

// Makes room for the book on a screen with no recipe book of its own. Such a screen places itself
// from its width, in init and again on every frame, so the width is what moves.
@Mixin(AbstractContainerScreen.class)
public abstract class ContainerScreenLayoutMixin
{
    @Shadow
    protected int imageWidth;

    @Inject(method = "init", at = @At("HEAD"))
    private void craftbound$makeRoomForBook(CallbackInfo ci)
    {
        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) (Object) this;
        if (RecipeBookScreenTweaks.docksBeside(screen))
            screen.width = RecipeBookScreenTweaks.besideScreenWidth(screen, imageWidth);
    }
}
