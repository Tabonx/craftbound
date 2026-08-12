package com.craftbound.client.progression;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import com.craftbound.client.jei.BookIngredient;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

// Vanilla's recipe toast, backed by book ingredients instead of RecipeHolders: the book unlocks
// recipes that came from JEI categories, which have no RecipeHolder to hand to net.minecraft's
// RecipeToast. Same sprite, same strings and the same cycling layout, so it reads as the popup
// players know — and each result is drawn with the renderer the book itself uses, so a fluid looks
// the same in both places.
public final class RecipeUnlockToast implements Toast
{
    private static final ResourceLocation BACKGROUND_SPRITE =
            ResourceLocation.withDefaultNamespace("toast/recipe");
    private static final Component TITLE_TEXT = Component.translatable("recipe.toast.title");
    private static final Component DESCRIPTION_TEXT = Component.translatable("recipe.toast.description");
    private static final long DISPLAY_TIME = 5000L;
    private static final int TITLE_COLOR = -11534256;
    private static final int DESCRIPTION_COLOR = -16777216;

    // Unlocking a whole branch at once can produce hundreds of results; past a handful the cycle is
    // a flicker rather than information, so the toast only ever shows the first few.
    private static final int MAX_ICONS = 8;

    private final List<BookIngredient> results = new ArrayList<>();
    private long lastChanged;
    private boolean changed;

    private RecipeUnlockToast(List<BookIngredient> results)
    {
        addAll(results);
        changed = true;
    }

    // Icons may be empty: an unlock whose only output has nothing drawable (a fluid with no bucket)
    // still deserves to be announced, so the toast falls back to its text alone rather than being
    // dropped.
    public static void addOrUpdate(ToastComponent toasts, List<BookIngredient> results)
    {
        RecipeUnlockToast existing = toasts.getToast(RecipeUnlockToast.class, NO_TOKEN);
        if (existing == null)
            toasts.addToast(new RecipeUnlockToast(results));
        else
            existing.addAll(results);
    }

    private void addAll(List<BookIngredient> results)
    {
        for (BookIngredient result : results)
        {
            if (this.results.size() >= MAX_ICONS)
                break;
            this.results.add(result);
            changed = true;
        }
    }

    @Override
    public Toast.Visibility render(GuiGraphics graphics, ToastComponent toasts, long timeSinceLastVisible)
    {
        if (changed)
        {
            lastChanged = timeSinceLastVisible;
            changed = false;
        }

        double displayTime = DISPLAY_TIME * toasts.getNotificationDisplayTimeMultiplier();
        graphics.blitSprite(BACKGROUND_SPRITE, 0, 0, width(), height());
        graphics.drawString(toasts.getMinecraft().font, TITLE_TEXT, 30, 7, TITLE_COLOR, false);
        graphics.drawString(toasts.getMinecraft().font, DESCRIPTION_TEXT, 30, 18, DESCRIPTION_COLOR, false);

        graphics.pose().pushPose();
        graphics.pose().scale(0.6F, 0.6F, 1.0F);
        graphics.renderFakeItem(new ItemStack(Items.CRAFTING_TABLE), 3, 3);
        graphics.pose().popPose();

        // Drawn through JEI's own renderer, so a fluid shows here exactly as it does in the book.
        if (!results.isEmpty())
            results.get(cycleIndex(timeSinceLastVisible, displayTime)).render(graphics, 8, 8);

        return timeSinceLastVisible - lastChanged >= displayTime ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
    }

    private int cycleIndex(long timeSinceLastVisible, double displayTime)
    {
        return (int) (timeSinceLastVisible / Math.max(1.0, displayTime / results.size()) % results.size());
    }
}
