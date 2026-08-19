package com.craftbound.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.ToastComponent;

// The toast queue moved from the client to its hud, so the book asks for it here rather than at
// every call site.
public final class Toasts
{
    public static ToastComponent of(Minecraft minecraft)
    {
        return minecraft.gui.toastManager();
    }

    private Toasts() {}
}
