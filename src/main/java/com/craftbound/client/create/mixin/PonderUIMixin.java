package com.craftbound.client.create.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.craftbound.client.create.PonderVisibility;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.createmod.ponder.foundation.ui.PonderUI;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

// Identify mode would name every part of a contraption the player has not reached yet, so
// undiscovered blocks answer with "???". The scene itself still plays.
@Mixin(PonderUI.class)
public class PonderUIMixin
{
    private static final Component UNKNOWN = Component.translatable("craftbound.ponder.unknown");

    @WrapOperation(
            method = "renderWidgets",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;renderTooltip(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;II)V"))
    private void craftbound$hideUndiscoveredName(GuiGraphics graphics, Font font, ItemStack stack,
            int x, int y, Operation<Void> original)
    {
        if (PonderVisibility.isUnnamed(stack.getItem()))
            graphics.renderTooltip(font, UNKNOWN, x, y);
        else
            original.call(graphics, font, stack, x, y);
    }
}
