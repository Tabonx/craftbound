package com.craftbound.client.progression;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.craftbound.CraftboundAttachments;
import com.craftbound.client.jei.BookIngredient;
import com.craftbound.client.jei.CraftboundJeiPlugin;
import com.craftbound.client.jei.RecipeIndexSnapshot;
import com.craftbound.progression.ProgressionConfig;
import com.craftbound.progression.ProgressionRules;
import com.craftbound.progression.RecipeIndex;
import com.craftbound.progression.RecipeNode;
import com.craftbound.progression.Unlocks;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

// Client-side view of what the player has unlocked. The obtained set arrives on its own as a synced
// attachment, so nothing here talks to the server; it only caches the expensive parts — the recipe
// index and the derived unlocked-output set — and rebuilds them when the inputs change.
//
// Staleness is judged by the obtained set's size: it only ever grows, so a changed size is exactly
// a changed set, and comparing sizes costs nothing per frame.
public final class Progression
{
    private static RecipeIndexSnapshot snapshot = RecipeIndexSnapshot.EMPTY;
    private static RecipeIndex index = RecipeIndex.EMPTY;
    private static ProgressionRules rules = ProgressionRules.OPEN;
    private static Set<String> unlockedOutputs = Set.of();
    private static Set<ResourceLocation> unlockingItems = Set.of();
    private static int obtainedSize = -1;

    // Unlocks not yet announced, and whether a baseline has been taken to measure them against.
    private static final Set<String> newlyUnlocked = new LinkedHashSet<>();
    private static boolean seeded = false;

    // Rebuilds the unlocked set if anything it depends on moved. Returns whether it changed, so the
    // book can re-apply its filter without polling the whole set.
    public static boolean refresh()
    {
        Set<ResourceLocation> obtained = obtained();
        ProgressionRules current = ProgressionConfig.rules();
        boolean indexStale = index.isEmpty() && CraftboundJeiPlugin.hasRuntime();

        if (!indexStale && obtained.size() == obtainedSize && current.equals(rules))
            return false;

        if (indexStale)
        {
            snapshot = CraftboundJeiPlugin.buildRecipeIndex();
            index = snapshot.index();
        }

        rules = current;
        obtainedSize = obtained.size();

        Set<String> previous = unlockedOutputs;
        unlockedOutputs = rules.enabled() ? Unlocks.unlockedOutputs(rules, index, obtained) : Set.of();
        unlockingItems = Unlocks.unlockingItems(rules, index, obtained, unlockedOutputs);
        recordNewlyUnlocked(previous);
        return true;
    }

    // The first pass over a real index establishes what the player already had; announcing all of it
    // would bury them in toasts on every world join. Only what unlocks afterwards is news.
    private static void recordNewlyUnlocked(Set<String> previous)
    {
        if (!seeded)
        {
            seeded = !index.isEmpty();
            return;
        }
        for (String key : unlockedOutputs)
        {
            if (!previous.contains(key))
                newlyUnlocked.add(key);
        }
    }

    // Drained rather than read, because the book's own refresh and the toast tick both drive
    // refresh() and either may be the one that notices an unlock.
    public static List<String> drainNewlyUnlocked()
    {
        if (newlyUnlocked.isEmpty())
            return List.of();

        List<String> keys = List.copyOf(newlyUnlocked);
        newlyUnlocked.clear();
        return keys;
    }

    // The ingredient behind an unlock key, so the toast can draw it with the same JEI renderer the
    // book uses instead of guessing at an item form.
    public static Optional<BookIngredient> displayFor(String unlockKey)
    {
        return snapshot.displayFor(unlockKey);
    }

    // Whether obtaining this item would reveal something the book is still hiding — what the grid
    // marks. Fluids and other ingredient types can never be obtained, so they are never marked.
    public static boolean unlocksMore(BookIngredient ingredient)
    {
        return ingredient.item()
                .map(item -> unlocksMore(BuiltInRegistries.ITEM.getKey(item)))
                .orElse(false);
    }

    public static boolean unlocksMore(ResourceLocation itemId)
    {
        return unlockingItems.contains(itemId);
    }

    public static boolean isUnlocked(BookIngredient ingredient)
    {
        if (!rules.enabled())
            return true;
        return unlockedOutputs.contains(ingredient.unlockKey());
    }

    // Judged within the category being shown: the same recipe object is listed by several
    // categories, and being locked as a mechanical-crafter recipe says nothing about it as a
    // crafting-table one. Recipes missing from the index (a category that failed to lay one out)
    // count as unlocked — hiding a recipe we could not read would make it unreachable forever.
    public static boolean isRecipeUnlocked(String categoryUid, Object recipe)
    {
        if (!rules.enabled())
            return true;
        RecipeNode node = index.node(categoryUid, recipe);
        return node == null || Unlocks.recipeUnlocked(rules, index, node, obtained(), unlockedOutputs);
    }

    public static boolean isCategoryUnlocked(String categoryUid)
    {
        if (!rules.enabled())
            return true;
        return Unlocks.categoryUnlocked(rules, categoryUid, index.catalystsFor(categoryUid), obtained());
    }

    // Recipes reload and world changes invalidate the index; it is rebuilt on the next refresh.
    public static void invalidate()
    {
        snapshot = RecipeIndexSnapshot.EMPTY;
        index = RecipeIndex.EMPTY;
        unlockedOutputs = Set.of();
        unlockingItems = Set.of();
        obtainedSize = -1;
        newlyUnlocked.clear();
        seeded = false;
    }

    private static Set<ResourceLocation> obtained()
    {
        LocalPlayer player = Minecraft.getInstance().player;
        return player == null ? Set.of() : player.getData(CraftboundAttachments.OBTAINED_ITEMS);
    }

    private Progression() {}
}
