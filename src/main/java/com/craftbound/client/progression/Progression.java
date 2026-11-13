package com.craftbound.client.progression;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.craftbound.client.session.ClientSession;
import com.craftbound.client.jei.BookIngredient;
import com.craftbound.client.jei.RecipeGate;
import com.craftbound.client.jei.RecipeIndexBuilder;

import net.minecraft.resources.ResourceLocation;

// The session's progression, for the book and the mixins that ask about it without holding a
// session of their own.
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

    public static List<String> drainNewlyUnlocked()
    {
        return cache().drainNewlyUnlocked();
    }

    public static boolean takeHighlight(BookIngredient ingredient)
    {
        return cache().takeHighlight(ingredient.unlockKey());
    }

    // The ingredient behind an unlock key, so the toast can draw it with the same JEI renderer the
    // book uses instead of guessing at an item form.
    public static Optional<BookIngredient> displayFor(String unlockKey)
    {
        return RecipeIndexBuilder.current().flatMap(snapshot -> snapshot.displayFor(unlockKey));
    }

    // Whether obtaining this item would reveal something the book is still hiding, which is what the
    // grid marks. Fluids and other ingredient types can never be obtained, so they are never marked.
    // The marks themselves are an upgrade, so they stay hidden until the book carries the lens.
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

    public static Set<String> outputsUsing(Set<String> inputKeys)
    {
        return cache().outputsUsing(inputKeys);
    }

    private static ProgressionCache cache()
    {
        return ClientSession.current().progression();
    }

    private Progression() {}
}
