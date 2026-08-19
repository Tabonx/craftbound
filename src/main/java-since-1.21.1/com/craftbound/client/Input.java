package com.craftbound.client;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;

// Newer versions hand widgets an event record where this generation passes loose coordinates and
// key codes. The book works in the loose form throughout, so its own input handling reads the same
// on every version.
public final class Input
{
    public static boolean click(GuiEventListener target, double mouseX, double mouseY, int button)
    {
        return target.mouseClicked(mouseX, mouseY, button);
    }

    public static boolean keyPressed(GuiEventListener target, int keyCode, int scanCode, int modifiers)
    {
        return target.keyPressed(keyCode, scanCode, modifiers);
    }

    public static boolean charTyped(GuiEventListener target, char codePoint, int modifiers)
    {
        return target.charTyped(codePoint, modifiers);
    }

    // Shift is asked of the window rather than the screen: newer versions only report it on the
    // input event, and the book needs it while drawing a tooltip too.
    public static boolean shiftDown()
    {
        long window = Minecraft.getInstance().getWindow().getWindow();
        return InputConstants.isKeyDown(window, InputConstants.KEY_LSHIFT)
                || InputConstants.isKeyDown(window, InputConstants.KEY_RSHIFT);
    }

    private Input() {}
}
