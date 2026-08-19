package com.craftbound.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

// The open screen moved off the client and onto its hud.
public final class Screens
{
    public static Screen current()
    {
        //? if >=26.2 {
        /*return Minecraft.getInstance().gui.screen();
        *///?} else {
        return Minecraft.getInstance().screen;
        //?}
    }

    private Screens() {}
}
