package com.craftbound.client.ponder.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.craftbound.client.ponder.PonderGate;

import net.createmod.ponder.foundation.PonderTooltipHandler;
import net.minecraft.world.item.ItemStack;

// Ponder's hold-to-ponder shortcut latches onto whatever stack a tooltip is being built for, which
// includes the book's own entries. Entries the player has never held drop the shortcut: no progress
// bar in the tooltip and no scene, since the tracked stack is cleared along with it.
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
        if (!PonderGate.blocks(stack))
            return;

        hoveredStack = ItemStack.EMPTY;
        trackingStack = ItemStack.EMPTY;
        ci.cancel();
    }
}
