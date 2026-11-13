package com.craftbound.client.progression;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.craftbound.client.jei.BookIngredient;
import com.craftbound.client.jei.RecipeGate;
import com.craftbound.client.jei.RecipeIndexBuilder;
import com.craftbound.client.session.ClientSession;
import com.craftbound.progression.UnlockKey;

import net.minecraft.resources.ResourceLocation;

public final class Progression
{
    public static void refresh()
    {
        cache().refresh();
    }

    public static int version()
    {
        return cache().version();
    }

    public static RecipeGate gate()
    {
        return cache();
    }

    public static List<UnlockKey> drainNewlyUnlocked()
    {
        return cache().drainNewlyUnlocked();
    }

    public static boolean takeHighlight(BookIngredient ingredient)
    {
        return cache().takeHighlight(ingredient.unlockKey());
    }

    public static Optional<BookIngredient> displayFor(UnlockKey unlockKey)
    {
        return RecipeIndexBuilder.current().flatMap(snapshot -> snapshot.displayFor(unlockKey));
    }

    // Fluids can never be obtained, so they are never marked. The marks stay hidden until the book
    // carries the lens upgrade.
    public static boolean unlocksMore(BookIngredient ingredient)
    {
        return ClientSession.current().bookUpgrade().hintsUnlocked()
                && BookIngredient.itemIdOf(ingredient.typed()).map(cache()::unlocksMore).orElse(false);
    }

    public static boolean isDiscovered(ResourceLocation itemId)
    {
        return cache().isDiscovered(itemId);
    }

    public static boolean canPonder(ResourceLocation itemId)
    {
        return cache().canPonder(itemId);
    }

    public static boolean isUnlocked(BookIngredient ingredient)
    {
        return cache().isUnlocked(ingredient.unlockKey());
    }

    public static String variantGroup(BookIngredient ingredient)
    {
        return cache().variantGroup(ingredient.unlockKey());
    }

    public static Set<UnlockKey> outputsUsing(Set<UnlockKey> inputKeys)
    {
        return cache().outputsUsing(inputKeys);
    }

    private static ProgressionCache cache()
    {
        return ClientSession.current().progression();
    }

    private Progression() {}
}
