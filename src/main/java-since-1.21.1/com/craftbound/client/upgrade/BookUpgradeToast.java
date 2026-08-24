package com.craftbound.client.upgrade;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;

// One call both draws the toast and decides whether it should still be shown.
public final class BookUpgradeToast extends BookUpgradeToastBase
{
    @Override
    public Toast.Visibility render(GuiGraphics graphics, ToastComponent toasts, long timeSinceLastVisible)
    {
        advance(toasts.getNotificationDisplayTimeMultiplier(), timeSinceLastVisible);
        draw(graphics, toasts.getMinecraft().font);
        return visibility;
    }
}
