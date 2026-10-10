package com.craftbound.client.mixin.jei;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.craftbound.client.jei.BookRecipeRender;

import mezz.jei.api.gui.ingredient.IRecipeSlotView;

import net.minecraft.client.gui.GuiGraphics;

// Tints a slot the player cannot fill between its background and its ingredient, the order
// vanilla draws a ghost recipe in. JEI asks for the displayed ingredient right before drawing it.
//
// JEI moved the body into draw(GuiGraphics, boolean) and renamed the getter it calls, so both
// overloads and both getters are targeted, and whichever the installed JEI has is tinted.
@Mixin(targets = "mezz.jei.library.gui.ingredients.RecipeSlot", remap = false)
public class MissingSlotMixin
{
    private static final String DISPLAYED =
            "Lmezz/jei/library/gui/ingredients/RecipeSlot;getDisplayedIngredient()Ljava/util/Optional;";
    private static final String DISPLAYED_SLOT =
            "Lmezz/jei/library/gui/ingredients/RecipeSlot;getDisplayedSlotIngredient()Ljava/util/Optional;";

    // Vanilla's ghost-recipe tint.
    private static final int MISSING = 0x30FF0000;

    @Inject(method = "draw(Lnet/minecraft/client/gui/GuiGraphics;)V", at = {
            @At(value = "INVOKE", target = DISPLAYED, ordinal = 0),
            @At(value = "INVOKE", target = DISPLAYED_SLOT, ordinal = 0)})
    private void craftbound$markMissing(GuiGraphics graphics, CallbackInfo ci)
    {
        mark(graphics);
    }

    @Inject(method = "draw(Lnet/minecraft/client/gui/GuiGraphics;Z)V", at = {
            @At(value = "INVOKE", target = DISPLAYED, ordinal = 0),
            @At(value = "INVOKE", target = DISPLAYED_SLOT, ordinal = 0)})
    private void craftbound$markMissing(GuiGraphics graphics, boolean hovered, CallbackInfo ci)
    {
        mark(graphics);
    }

    private void mark(GuiGraphics graphics)
    {
        if ((Object) this instanceof IRecipeSlotView slot && BookRecipeRender.isMissing(slot))
            slot.drawHighlight(graphics, MISSING);
    }
}
