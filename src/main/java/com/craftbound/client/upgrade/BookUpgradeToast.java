package com.craftbound.client.upgrade;

import com.craftbound.CraftboundItems;
import com.craftbound.client.Canvas;
import com.craftbound.client.VersionedToast;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

// Announces that the lens has been bound, in the shape of vanilla's recipe toast so it reads as
// part of the book rather than as a new kind of popup.
public final class BookUpgradeToast extends VersionedToast
{
    private static final ResourceLocation BACKGROUND_SPRITE = ResourceLocation.withDefaultNamespace("toast/recipe");
    private static final Component TITLE_TEXT = Component.translatable("craftbound.upgrade.toast.title");
    private static final Component DESCRIPTION_TEXT = Component.translatable("craftbound.upgrade.toast.description");
    private static final long DISPLAY_TIME = 5000L;
    private static final int TITLE_COLOR = -11534256;
    private static final int DESCRIPTION_COLOR = -16777216;

    private long lastChanged;
    private boolean changed = true;

    public static void show()
    {
        Minecraft.getInstance().getToasts().addToast(new BookUpgradeToast());
    }

    @Override
    protected Toast.Visibility advance(double displayTimeMultiplier, long fullyVisibleForMs)
    {
        if (changed)
        {
            lastChanged = fullyVisibleForMs;
            changed = false;
        }

        return fullyVisibleForMs - lastChanged >= DISPLAY_TIME * displayTimeMultiplier
                ? Toast.Visibility.HIDE
                : Toast.Visibility.SHOW;
    }

    @Override
    protected void draw(GuiGraphics graphics, Font font, long fullyVisibleForMs)
    {
        Canvas.sprite(graphics, BACKGROUND_SPRITE, 0, 0, width(), height());
        graphics.drawString(font, TITLE_TEXT, 30, 7, TITLE_COLOR, false);
        graphics.drawString(font, DESCRIPTION_TEXT, 30, 18, DESCRIPTION_COLOR, false);
        graphics.renderFakeItem(new ItemStack(CraftboundItems.BOOKBINDERS_LENS.get()), 8, 8);
    }
}
