package com.craftbound.client.compat;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;

// A toast told how long it has been fully visible, first to decide whether it stays and then to
// draw. This generation asks both in one call.
public abstract class VersionedToast implements Toast
{
    @Override
    public Toast.Visibility render(GuiGraphics graphics, ToastComponent toasts, long fullyVisibleForMs)
    {
        Toast.Visibility visibility = advance(toasts.getNotificationDisplayTimeMultiplier(), fullyVisibleForMs);
        draw(graphics, toasts.getMinecraft().font, fullyVisibleForMs);
        return visibility;
    }

    protected abstract Toast.Visibility advance(double displayTimeMultiplier, long fullyVisibleForMs);

    protected abstract void draw(GuiGraphics graphics, Font font, long fullyVisibleForMs);
}
