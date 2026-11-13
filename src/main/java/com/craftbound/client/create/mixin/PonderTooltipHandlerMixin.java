package com.craftbound.client.create.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.craftbound.client.create.PonderVisibility;

import net.createmod.ponder.foundation.PonderTooltipHandler;
import net.minecraft.world.item.ItemStack;

// Ponder's hold-to-ponder shortcut latches onto any stack a tooltip is built for, including the
// book's entries. Clearing the tracked stack drops both the progress bar and the scene.
@Mixin(PonderTooltipHandler.class)
public class PonderTooltipHandlerMixin
{
    @Shadow
    static ItemStack hoveredStack;

    @Shadow
    static ItemStack trackingStack;

    @Inject(method = "updateHovered", at = @At("HEAD"), cancellable = true)
    private static void craftbound$gateBookEntries(ItemStack stack, CallbackInfo ci)
    {
        if (!PonderVisibility.blocksShortcut(stack))
            return;

        hoveredStack = ItemStack.EMPTY;
        trackingStack = ItemStack.EMPTY;
        ci.cancel();
    }
}
