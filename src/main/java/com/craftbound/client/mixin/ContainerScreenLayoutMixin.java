package com.craftbound.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.craftbound.client.RecipeBookLayout;
import com.craftbound.client.RecipeBookScreenTweaks;
import com.craftbound.client.RecipeBookState;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

// Makes room for the book on a screen with no recipe book of its own. Such a screen places itself
// from its width, in init and again on every frame, so the width is what moves. It is taken from
// the window each time, as a screen rebuilt without a resize keeps the width it was given.
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
            screen.width = RecipeBookLayout.besideScreenWidth(
                    Minecraft.getInstance().getWindow().getGuiScaledWidth(), imageWidth, RecipeBookState.isOpen());
    }
}
