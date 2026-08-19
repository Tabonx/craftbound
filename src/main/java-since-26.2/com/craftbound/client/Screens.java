package com.craftbound.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

// The open screen moved off the client and onto its hud.
public final class Screens
{
    public static Screen current()
    {
        return Minecraft.getInstance().gui.screen();
    }

    private Screens() {}
}
