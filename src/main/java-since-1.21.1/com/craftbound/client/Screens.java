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

    // Lays the screen out again from scratch, Init events included, as a window resize does. The
    // size comes from the window, since a screen beside the book was told a width of its own.
    public static void rebuild(Screen screen)
    {
        var window = Minecraft.getInstance().getWindow();
        //? if >=1.21.11 {
        /*screen.resize(window.getGuiScaledWidth(), window.getGuiScaledHeight());
        *///?} else {
        screen.resize(Minecraft.getInstance(), window.getGuiScaledWidth(), window.getGuiScaledHeight());
        //?}
    }

    private Screens() {}
}
