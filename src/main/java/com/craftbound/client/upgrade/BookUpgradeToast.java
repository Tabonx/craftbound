package com.craftbound.client.upgrade;

import com.craftbound.client.Canvas;
import com.craftbound.client.Toasts;
import com.craftbound.CraftboundItems;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

// Announces that the lens has been bound, in the shape of vanilla's recipe toast so it reads as
// part of the book rather than as a new kind of popup.
public final class BookUpgradeToast implements Toast
{
    private static final ResourceLocation BACKGROUND_SPRITE =
            ResourceLocation.withDefaultNamespace("toast/recipe");
    private static final Component TITLE_TEXT = Component.translatable("craftbound.upgrade.toast.title");
    private static final Component DESCRIPTION_TEXT =
            Component.translatable("craftbound.upgrade.toast.description");
    private static final long DISPLAY_TIME = 5000L;
    private static final int TITLE_COLOR = -11534256;
    private static final int DESCRIPTION_COLOR = -16777216;

    private long lastChanged;
    private boolean changed = true;

    public static void show()
    {
        Toasts.of(Minecraft.getInstance()).addToast(new BookUpgradeToast());
    }

    //? if >=1.21.5 {
    /*// Newer versions ask a toast what it wants, tick it, and draw it in three separate calls
    // rather than one that both draws and decides.
    private Toast.Visibility visibility = Toast.Visibility.SHOW;

    @Override
    public Toast.Visibility getWantedVisibility()
    {
        return visibility;
    }

    @Override
    public void update(ToastComponent toasts, long fullyVisibleForMs)
    {
        visibility = fullyVisibleForMs >= DISPLAY_TIME * toasts.getNotificationDisplayTimeMultiplier()
                ? Toast.Visibility.HIDE
                : Toast.Visibility.SHOW;
    }

    @Override
    public void extractRenderState(GuiGraphics graphics, Font font, long fullyVisibleForMs)
    {
        Canvas.sprite(graphics, BACKGROUND_SPRITE, 0, 0, width(), height());
        graphics.drawString(font, TITLE_TEXT, 30, 7, TITLE_COLOR, false);
        graphics.drawString(font, DESCRIPTION_TEXT, 30, 18, DESCRIPTION_COLOR, false);
        graphics.renderFakeItem(new ItemStack(CraftboundItems.BOOKBINDERS_LENS.get()), 8, 8);
    }
    *///?} else {
    @Override
    public Toast.Visibility render(GuiGraphics graphics, ToastComponent toasts, long timeSinceLastVisible)
    {
        if (changed)
        {
            lastChanged = timeSinceLastVisible;
            changed = false;
        }

        double displayTime = DISPLAY_TIME * toasts.getNotificationDisplayTimeMultiplier();
        Canvas.sprite(graphics, BACKGROUND_SPRITE, 0, 0, width(), height());
        graphics.drawString(toasts.getMinecraft().font, TITLE_TEXT, 30, 7, TITLE_COLOR, false);
        graphics.drawString(toasts.getMinecraft().font, DESCRIPTION_TEXT, 30, 18, DESCRIPTION_COLOR, false);
        graphics.renderFakeItem(new ItemStack(CraftboundItems.BOOKBINDERS_LENS.get()), 8, 8);

        return timeSinceLastVisible - lastChanged >= displayTime ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
    }
    //?}
}
