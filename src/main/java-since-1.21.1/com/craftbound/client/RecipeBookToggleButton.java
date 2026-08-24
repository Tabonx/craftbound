package com.craftbound.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;

public final class RecipeBookToggleButton extends RecipeBookToggleButtonBase
{
    public RecipeBookToggleButton(int x, int y, Button.OnPress onPress)
    {
        super(x, y, onPress);
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        super.renderWidget(graphics, mouseX, mouseY, partialTick);
        drawUpgradeHint(graphics);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        return unbindOr(mouseX, mouseY, button, () -> super.mouseClicked(mouseX, mouseY, button));
    }
}
