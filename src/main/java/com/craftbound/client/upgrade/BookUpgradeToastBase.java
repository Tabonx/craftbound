package com.craftbound.client.upgrade;

import com.craftbound.CraftboundItems;
import com.craftbound.client.Canvas;
import com.craftbound.client.Toasts;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

// Announces that the lens has been bound, in the shape of vanilla's recipe toast so it reads as
// part of the book rather than as a new kind of popup.
//
// What the toast is and how long it stays are here; how a version asks it to draw and to decide
// is on BookUpgradeToast.
public abstract class BookUpgradeToastBase implements Toast
{
    protected static final ResourceLocation BACKGROUND_SPRITE =
            ResourceLocation.withDefaultNamespace("toast/recipe");
    protected static final Component TITLE_TEXT = Component.translatable("craftbound.upgrade.toast.title");
    protected static final Component DESCRIPTION_TEXT =
            Component.translatable("craftbound.upgrade.toast.description");
    protected static final long DISPLAY_TIME = 5000L;
    protected static final int TITLE_COLOR = -11534256;
    protected static final int DESCRIPTION_COLOR = -16777216;

    protected Toast.Visibility visibility = Toast.Visibility.SHOW;
    private long lastChanged;
    private boolean changed = true;

    public static void show()
    {
        Toasts.of(Minecraft.getInstance()).addToast(new BookUpgradeToast());
    }

    protected void advance(double displayTimeMultiplier, long fullyVisibleForMs)
    {
        if (changed)
        {
            lastChanged = fullyVisibleForMs;
            changed = false;
        }

        visibility = fullyVisibleForMs - lastChanged >= DISPLAY_TIME * displayTimeMultiplier
                ? Toast.Visibility.HIDE
                : Toast.Visibility.SHOW;
    }

    protected void draw(GuiGraphics graphics, Font font)
    {
        Canvas.sprite(graphics, BACKGROUND_SPRITE, 0, 0, width(), height());
        graphics.drawString(font, TITLE_TEXT, 30, 7, TITLE_COLOR, false);
        graphics.drawString(font, DESCRIPTION_TEXT, 30, 18, DESCRIPTION_COLOR, false);
        graphics.renderFakeItem(new ItemStack(CraftboundItems.BOOKBINDERS_LENS.get()), 8, 8);
    }
}
