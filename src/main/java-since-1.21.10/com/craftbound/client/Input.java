package com.craftbound.client;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;

import org.lwjgl.glfw.GLFW;

// This generation hands widgets an event record where older ones passed loose coordinates and key
// codes. The book works in the loose form throughout and packs an event only when handing input on
// to a vanilla widget, so its own input handling reads the same on every version.
public final class Input
{
    public static boolean click(GuiEventListener target, double mouseX, double mouseY, int button)
    {
        return click(target, mouseX, mouseY, button, false);
    }

    // Shift goes along because a text field reads it off the event to extend its selection, and a
    // double click selects the word under the cursor.
    public static boolean click(GuiEventListener target, double mouseX, double mouseY, int button,
            boolean doubleClick)
    {
        int modifiers = shiftDown() ? GLFW.GLFW_MOD_SHIFT : 0;
        return target.mouseClicked(new MouseButtonEvent(mouseX, mouseY, new MouseButtonInfo(button, modifiers)),
                doubleClick);
    }

    public static boolean keyPressed(GuiEventListener target, int keyCode, int scanCode, int modifiers)
    {
        return target.keyPressed(new KeyEvent(keyCode, scanCode, modifiers));
    }

    public static boolean charTyped(GuiEventListener target, char codePoint, int modifiers)
    {
        //? if >=26.1 {
        /*return target.charTyped(new CharacterEvent(codePoint));
        *///?} else {
        return target.charTyped(new CharacterEvent(codePoint, modifiers));
        //?}
    }

    // Shift is asked of the window rather than the screen: this generation only reports it on the
    // input event, and the book needs it while drawing a tooltip too.
    public static boolean shiftDown()
    {
        var window = Minecraft.getInstance().getWindow();
        return InputConstants.isKeyDown(window, InputConstants.KEY_LSHIFT)
                || InputConstants.isKeyDown(window, InputConstants.KEY_RSHIFT);
    }

    private Input() {}
}
