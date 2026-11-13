package com.craftbound.client.compat;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;

// A toast told how long it has been fully visible, first to decide whether it stays and then to
// draw. This generation asks what it wants, ticks it, and draws it in three separate calls.
public abstract class VersionedToast implements Toast
{
    private Toast.Visibility visibility = Toast.Visibility.SHOW;

    @Override
    public Toast.Visibility getWantedVisibility()
    {
        return visibility;
    }

    @Override
    public void update(ToastComponent toasts, long fullyVisibleForMs)
    {
        visibility = advance(toasts.getNotificationDisplayTimeMultiplier(), fullyVisibleForMs);
    }

    //? if >=26.1 {
    /*@Override
    public void extractRenderState(GuiGraphics graphics, Font font, long fullyVisibleForMs)
    *///?} else {
    @Override
    public void render(GuiGraphics graphics, Font font, long fullyVisibleForMs)
    //?}
    {
        draw(graphics, font, fullyVisibleForMs);
    }

    protected abstract Toast.Visibility advance(double displayTimeMultiplier, long fullyVisibleForMs);

    protected abstract void draw(GuiGraphics graphics, Font font, long fullyVisibleForMs);
}
