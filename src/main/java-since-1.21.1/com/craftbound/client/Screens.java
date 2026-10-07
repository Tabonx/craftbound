package com.craftbound.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

// The open screen later moved off the client and onto its hud.
public final class Screens
{
    public static Screen current()
    {
        return Minecraft.getInstance().screen;
    }

    // Lays the screen out again from scratch, Init events included, as a window resize does.
    public static void rebuild(Screen screen)
    {
        //? if >=1.21.11 {
        /*screen.resize(screen.width, screen.height);
        *///?} else {
        screen.resize(Minecraft.getInstance(), screen.width, screen.height);
        //?}
    }

    private Screens() {}
}
