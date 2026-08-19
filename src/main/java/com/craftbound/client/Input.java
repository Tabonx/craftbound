package com.craftbound.client;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
//? if >=1.21.5 {
/*import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
*///?}

// Newer versions hand widgets an event record where older ones passed loose coordinates and key
// codes. The book works in the loose form throughout and packs an event only when handing input on
// to a vanilla widget, so its own input handling reads the same on every version.
public final class Input
{
    public static boolean click(GuiEventListener target, double mouseX, double mouseY, int button)
    {
        //? if >=1.21.5 {
        /*return target.mouseClicked(new MouseButtonEvent(mouseX, mouseY, new MouseButtonInfo(button, 0)), false);
        *///?} else {
        return target.mouseClicked(mouseX, mouseY, button);
        //?}
    }

    public static boolean keyPressed(GuiEventListener target, int keyCode, int scanCode, int modifiers)
    {
        //? if >=1.21.5 {
        /*return target.keyPressed(new KeyEvent(keyCode, scanCode, modifiers));
        *///?} else {
        return target.keyPressed(keyCode, scanCode, modifiers);
        //?}
    }

    public static boolean charTyped(GuiEventListener target, char codePoint, int modifiers)
    {
        //? if >=26.1 {
        /*return target.charTyped(new CharacterEvent(codePoint));
        *///?} elif >=1.21.5 {
        /*return target.charTyped(new CharacterEvent(codePoint, modifiers));
        *///?} else {
        return target.charTyped(codePoint, modifiers);
        //?}
    }

    // Shift is asked of the window rather than the screen: newer versions only report it on the
    // input event, and the book needs it while drawing a tooltip too.
    public static boolean shiftDown()
    {
        //? if >=1.21.5 {
        /*var window = Minecraft.getInstance().getWindow();
        *///?} else {
        long window = Minecraft.getInstance().getWindow().getWindow();
        //?}
        return InputConstants.isKeyDown(window, InputConstants.KEY_LSHIFT)
                || InputConstants.isKeyDown(window, InputConstants.KEY_RSHIFT);
    }

    private Input() {}
}
