package com.craftbound.client;

import java.util.List;

import com.craftbound.client.jei.BookIngredient;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

// The panel listing a grid entry's variants, vanilla's recipe overlay: rows of four (five past
// sixteen), opened over the clicked entry and moved back toward the book's middle a slot at a time
// where it would run off it, by the same limits vanilla uses.
final class VariantsOverlay
{
    private static final ResourceLocation PANEL = ResourceLocation.withDefaultNamespace("recipe_book/overlay_recipe");
    private static final int SLOT = 25;
    private static final int PADDING_X = 4;
    private static final int PADDING_Y = 5;

    @FunctionalInterface
    interface SlotDrawer
    {
        void draw(BookIngredient variant, int x, int y);
    }

    private List<BookIngredient> variants = List.of();
    private int x;
    private int y;

    void open(List<BookIngredient> variants, int slotX, int slotY, int centerX, int centerY)
    {
        this.variants = variants;
        x = slotX;
        y = slotY;

        int right = x + Math.min(variants.size(), columns()) * SLOT;
        if (right > centerX + 50)
            x -= SLOT * ((right - centerX - 50) / SLOT);

        int bottom = y + rows() * SLOT;
        if (bottom > centerY + 50)
            y -= SLOT * (int) Math.ceil((bottom - centerY - 50) / (double) SLOT);
        if (y < centerY - 100)
            y -= SLOT * (int) Math.ceil((y - centerY + 100) / (double) SLOT);
    }

    void close()
    {
        variants = List.of();
    }

    boolean isOpen()
    {
        return !variants.isEmpty();
    }

    BookIngredient variantAt(double mouseX, double mouseY)
    {
        for (int i = 0; i < variants.size(); i++)
        {
            int slotX = slotX(i);
            int slotY = slotY(i);
            if (mouseX >= slotX && mouseX < slotX + SLOT && mouseY >= slotY && mouseY < slotY + SLOT)
                return variants.get(i);
        }
        return null;
    }

    void render(GuiGraphics graphics, SlotDrawer slots)
    {
        if (!isOpen())
            return;

        Canvas.inFront(graphics, () ->
        {
            Canvas.sprite(graphics, PANEL, x, y, Math.min(variants.size(), columns()) * SLOT + 8, rows() * SLOT + 8);
            for (int i = 0; i < variants.size(); i++)
                slots.draw(variants.get(i), slotX(i), slotY(i));
        });
    }

    private int columns()
    {
        return variants.size() <= 16 ? 4 : 5;
    }

    private int rows()
    {
        return (variants.size() + columns() - 1) / columns();
    }

    private int slotX(int index)
    {
        return x + PADDING_X + SLOT * (index % columns());
    }

    private int slotY(int index)
    {
        return y + PADDING_Y + SLOT * (index / columns());
    }
}
