package com.craftbound.client.upgrade;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;

// The toast is asked what it wants, ticked, and drawn in three separate calls, the drawing one
// named after the render state it now extracts.
public final class BookUpgradeToast extends BookUpgradeToastBase
{
    @Override
    public Toast.Visibility getWantedVisibility()
    {
        return visibility;
    }

    @Override
    public void update(ToastComponent toasts, long fullyVisibleForMs)
    {
        advance(toasts.getNotificationDisplayTimeMultiplier(), fullyVisibleForMs);
    }

    @Override
    public void extractRenderState(GuiGraphics graphics, Font font, long fullyVisibleForMs)
    {
        draw(graphics, font);
    }
}
