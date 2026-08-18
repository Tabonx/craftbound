package com.craftbound.client.jei;

import java.util.Collection;
import java.util.List;

import mezz.jei.api.gui.handlers.IGlobalGuiHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Rect2i;

// Keeps JEI's own overlays off the screen. Craftbound uses JEI as the thing that knows every mod's
// recipes, not as a second interface: its ingredient list shows every item in the pack at once,
// which is the opposite of a book that reveals things as they are found.
//
// JEI shows an overlay only where it has room, and a global gui handler is how a mod tells JEI
// which parts of the screen are spoken for. Claiming the whole screen leaves no room, so both the
// ingredient list and the bookmark list stay hidden without touching the player's JEI settings.
final class JeiOverlayHider implements IGlobalGuiHandler
{
    @Override
    public Collection<Rect2i> getGuiExtraAreas()
    {
        var window = Minecraft.getInstance().getWindow();
        return List.of(new Rect2i(0, 0, window.getGuiScaledWidth(), window.getGuiScaledHeight()));
    }
}
