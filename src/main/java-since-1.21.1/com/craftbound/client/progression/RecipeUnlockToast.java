package com.craftbound.client.progression;

import java.util.List;

import com.craftbound.client.jei.BookIngredient;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;

// One call both draws the toast and decides whether it should still be shown.
public final class RecipeUnlockToast extends RecipeUnlockToastBase
{
    RecipeUnlockToast(List<BookIngredient> results)
    {
        super(results);
    }

    @Override
    public Toast.Visibility render(GuiGraphics graphics, ToastComponent toasts, long timeSinceLastVisible)
    {
        advance(toasts.getNotificationDisplayTimeMultiplier(), timeSinceLastVisible);
        draw(graphics, toasts.getMinecraft().font, timeSinceLastVisible);
        return visibility;
    }
}
