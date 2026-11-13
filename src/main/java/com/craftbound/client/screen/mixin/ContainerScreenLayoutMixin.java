package com.craftbound.client.screen.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.craftbound.client.screen.BookScreens;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

// Makes room for the book on a screen with no recipe book of its own. Such a screen places itself
// from its width, in init and again on every frame, so the width is what moves. Any
// container screen is noted here as the one laying itself out: a recipe-book screen asks its book
// where to stand right after.
@Mixin(AbstractContainerScreen.class)
public abstract class ContainerScreenLayoutMixin
{
    @Shadow
    protected int imageWidth;

    @Inject(method = "init", at = @At("HEAD"))
    private void craftbound$makeRoomForBook(CallbackInfo ci)
    {
        BookScreens.layingOut((AbstractContainerScreen<?>) (Object) this, imageWidth);
    }
}
