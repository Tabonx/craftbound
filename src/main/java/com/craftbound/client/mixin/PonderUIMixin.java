package com.craftbound.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.craftbound.client.ponder.PonderVisibility;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.createmod.ponder.foundation.ui.PonderUI;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

// Identify mode names whatever block the cursor is over inside a scene, which hands the player the
// name of every part in a contraption they have not reached yet. Undiscovered blocks answer with
// "???" instead; the scene still plays, so what the machine *does* is never hidden, only what the
// pieces are called.
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
        if (PonderVisibility.isHidden(stack.getItem()))
            graphics.renderTooltip(font, UNKNOWN, x, y);
        else
            original.call(graphics, font, stack, x, y);
    }
}
