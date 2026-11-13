package com.craftbound.client.progression;

import java.util.List;
import java.util.Optional;

import com.craftbound.client.jei.BookIngredient;

import net.minecraft.client.Minecraft;

// Announces newly unlocked recipes with vanilla's recipe toast. Unlocks are noticed on the client
// tick rather than in the book because they happen while the player is out mining, not while they
// are reading: the book only looks while it is open.
public final class ProgressionToasts
{
    public static void announce(ProgressionCache progression)
    {
        progression.refresh();

        List<String> unlocked = progression.drainNewlyUnlocked();
        if (unlocked.isEmpty())
            return;

        // Anything with no drawable form still counts as an unlock; the toast just shows fewer
        // icons rather than not appearing.
        List<BookIngredient> icons = unlocked.stream()
                .map(Progression::displayFor)
                .flatMap(Optional::stream)
                .toList();

        RecipeUnlockToast.addOrUpdate(Minecraft.getInstance().getToasts(), icons);
    }

    private ProgressionToasts() {}
}
