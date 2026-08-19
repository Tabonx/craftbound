package com.craftbound.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.MouseButtonEvent;

public final class RecipeBookToggleButton extends RecipeBookToggleButtonBase
{
    public RecipeBookToggleButton(int x, int y, Button.OnPress onPress)
    {
        super(x, y, onPress);
    }

    @Override
    public void extractContents(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        super.extractContents(graphics, mouseX, mouseY, partialTick);
        drawUpgradeHint(graphics);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick)
    {
        return unbindOr(event.x(), event.y(), event.button(), () -> super.mouseClicked(event, doubleClick));
    }
}
