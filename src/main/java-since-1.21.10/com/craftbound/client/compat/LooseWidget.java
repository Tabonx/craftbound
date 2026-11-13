package com.craftbound.client.compat;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

// Unpacks this generation's input event records into loose coordinates and key codes.
public abstract class LooseWidget extends AbstractWidget
{
    protected LooseWidget(int x, int y, int width, int height, Component message)
    {
        super(x, y, width, height, message);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick)
    {
        return mousePressed(event.x(), event.y(), event.button(), doubleClick);
    }

    @Override
    public boolean keyPressed(KeyEvent event)
    {
        return keyDown(event.key(), event.scancode(), event.modifiers());
    }

    @Override
    public boolean charTyped(CharacterEvent event)
    {
        return typed(event.codepoint(), 0);
    }

    protected abstract boolean mousePressed(double mouseX, double mouseY, int button, boolean doubleClick);

    protected abstract boolean keyDown(int keyCode, int scanCode, int modifiers);

    protected abstract boolean typed(int codePoint, int modifiers);
}
