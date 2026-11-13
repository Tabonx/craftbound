package com.craftbound.client.compat;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;

// The book takes input as loose coordinates and key codes on every version.
public final class Input
{
    public static boolean click(GuiEventListener target, double mouseX, double mouseY, int button)
    {
        return click(target, mouseX, mouseY, button, false);
    }

    // This generation has no double click: widgets are never told of one.
    public static boolean click(GuiEventListener target, double mouseX, double mouseY, int button,
            boolean doubleClick)
    {
        return target.mouseClicked(mouseX, mouseY, button);
    }

    public static boolean keyPressed(GuiEventListener target, int keyCode, int scanCode, int modifiers)
    {
        return target.keyPressed(keyCode, scanCode, modifiers);
    }

    // This generation types a character outside the basic plane as its two halves, one after the other.
    public static boolean charTyped(GuiEventListener target, int codePoint, int modifiers)
    {
        boolean taken = false;
        for (char half : Character.toChars(codePoint))
            taken |= target.charTyped(half, modifiers);
        return taken;
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
