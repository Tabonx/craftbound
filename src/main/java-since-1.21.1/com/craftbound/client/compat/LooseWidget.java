package com.craftbound.client.compat;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;

// A widget that takes input as loose coordinates and key codes, the form the book works in on every
// version. This generation hands them over that way already.
public abstract class LooseWidget extends AbstractWidget
{
    protected LooseWidget(int x, int y, int width, int height, Component message)
    {
        super(x, y, width, height, message);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        return mousePressed(mouseX, mouseY, button, false);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        return keyDown(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers)
    {
        return typed(codePoint, modifiers);
    }

    protected abstract boolean mousePressed(double mouseX, double mouseY, int button, boolean doubleClick);

    protected abstract boolean keyDown(int keyCode, int scanCode, int modifiers);

    protected abstract boolean typed(int codePoint, int modifiers);
}
