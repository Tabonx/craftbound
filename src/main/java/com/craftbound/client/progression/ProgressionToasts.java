package com.craftbound.client.progression;

import java.util.List;
import java.util.Optional;

import com.craftbound.client.jei.BookIngredient;
import com.craftbound.progression.UnlockKey;

import net.minecraft.client.Minecraft;

// Polled on the client tick rather than in the book, since unlocks happen while the book is closed.
public final class ProgressionToasts
{
    public static void announce(ProgressionCache progression)
    {
        progression.refresh();

        List<UnlockKey> unlocked = progression.drainNewlyUnlocked();
        if (unlocked.isEmpty())
            return;

        List<BookIngredient> icons = unlocked.stream()
                .map(Progression::displayFor)
                .flatMap(Optional::stream)
                .toList();

        RecipeUnlockToast.addOrUpdate(Minecraft.getInstance().getToasts(), icons);
    }

    private ProgressionToasts() {}
}
