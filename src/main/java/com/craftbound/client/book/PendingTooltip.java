package com.craftbound.client.book;

import java.util.List;
import java.util.function.Supplier;

import com.craftbound.client.compat.Canvas;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

// The tooltip a part of the book wants drawn this frame, held until the screen is done so it covers
// the screen's own slots as well.
@FunctionalInterface
interface PendingTooltip
{
    PendingTooltip NONE = (graphics, font, mouseX, mouseY) -> { };

    void draw(GuiGraphics graphics, Font font, int mouseX, int mouseY);

    static PendingTooltip of(Component text)
    {
        return (graphics, font, mouseX, mouseY) -> Canvas.tooltip(graphics, font, text, mouseX, mouseY);
    }

    static PendingTooltip lines(Supplier<List<Component>> lines)
    {
        return (graphics, font, mouseX, mouseY) -> graphics.renderComponentTooltip(font, lines.get(), mouseX, mouseY);
    }
}
