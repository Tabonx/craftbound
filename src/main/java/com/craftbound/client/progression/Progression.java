package com.craftbound.client.progression;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.craftbound.client.jei.BookIngredient;
import com.craftbound.client.jei.CraftboundJeiPlugin;
import com.craftbound.client.jei.RecipeIndexSnapshot;
import com.craftbound.client.upgrade.ClientBookUpgrade;
import com.craftbound.progression.ProgressionConfig;
import com.craftbound.progression.ProgressionRules;
import com.craftbound.progression.RecipeIndex;
import com.craftbound.progression.RecipeNode;
import com.craftbound.progression.UnlockKey;
import com.craftbound.progression.Unlocks;
import com.craftbound.progression.VariantGroups;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

// Client-side view of what the player has unlocked. The obtained set comes from
// ClientObtainedItems, so nothing here talks to the server; it only caches the expensive parts, the
// recipe index and the derived unlocked-output set, and rebuilds them when the inputs change.
//
// Staleness is judged by the obtained set's size: it only ever grows, so a changed size is exactly
// a changed set, and comparing sizes costs nothing per frame.
public final class Progression
{
    private static RecipeIndexSnapshot snapshot = RecipeIndexSnapshot.EMPTY;
    private static RecipeIndex index = RecipeIndex.EMPTY;
    private static Map<String, String> variantGroups = Map.of();
    private static ProgressionRules rules = ProgressionRules.OPEN;
    private static Set<String> unlockedOutputs = Set.of();
    // The same with subtypes dropped, for questions asked of a whole item: one unlocked potion is
    // enough to know potions exist.
    private static Set<String> unlockedRegistryKeys = Set.of();
    // What recipe inputs are checked against: the unlocked outputs plus the variants held, so a held
    // water bottle satisfies the slots that ask for one without giving it an entry of its own.
    private static Set<String> available = Set.of();
    private static int heldVariantsSize = -1;
    private static Set<ResourceLocation> unlockingItems = Set.of();
    private static int obtainedSize = -1;

    // Unlocks not yet announced, and whether a baseline has been taken to measure them against.
    private static final Set<String> newlyUnlocked = new LinkedHashSet<>();
    private static boolean seeded = false;

    // Unlocks the player has not yet laid eyes on in the book. Kept apart from newlyUnlocked, which
    // the toast drains as soon as it fires; this one survives until the grid actually shows the
    // entry, the way vanilla holds a highlight until the recipe appears on a page.
    private static final Set<String> highlighted = new LinkedHashSet<>();

    // Rebuilds the unlocked set if anything it depends on moved. Returns whether it changed, so the
    // book can re-apply its filter without polling the whole set.
    public static boolean refresh()
    {
        Set<ResourceLocation> obtained = obtained();
        Set<String> heldVariants = ClientHeldVariants.current();
        ProgressionRules current = ProgressionConfig.rules();
        boolean indexStale = index.isEmpty() && CraftboundJeiPlugin.hasRuntime();

        if (!indexStale && obtained.size() == obtainedSize && heldVariants.size() == heldVariantsSize
                && current.equals(rules))
            return false;

        if (indexStale)
        {
            snapshot = CraftboundJeiPlugin.buildRecipeIndex();
            index = snapshot.index();
            variantGroups = VariantGroups.of(index);
        }

        rules = current;
        obtainedSize = obtained.size();
        heldVariantsSize = heldVariants.size();

        Set<String> previous = unlockedOutputs;
        unlockedOutputs = rules.enabled() ? Unlocks.unlockedOutputs(rules, index, obtained, heldVariants) : Set.of();
        unlockedRegistryKeys = unlockedOutputs.stream().map(UnlockKey::withoutSubtype).collect(Collectors.toSet());
        available = new HashSet<>(unlockedOutputs);
        available.addAll(heldVariants);
        unlockingItems = Unlocks.unlockingItems(rules, index, obtained, available);
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
            {
                newlyUnlocked.add(key);
                highlighted.add(key);
            }
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

    // Claims an entry's highlight: true once, for the first render that shows it, which then plays
    // the animation on its own clock.
    public static boolean takeHighlight(BookIngredient ingredient)
    {
        return highlighted.remove(ingredient.unlockKey());
    }

    // The ingredient behind an unlock key, so the toast can draw it with the same JEI renderer the
    // book uses instead of guessing at an item form.
    public static Optional<BookIngredient> displayFor(String unlockKey)
    {
        return snapshot.displayFor(unlockKey);
    }

    // Whether obtaining this item would reveal something the book is still hiding, which is what the grid
    // marks. Fluids and other ingredient types can never be obtained, so they are never marked. The
    // marks themselves are an upgrade, so they stay hidden until the book carries the lens.
    public static boolean unlocksMore(BookIngredient ingredient)
    {
        return ingredient.item()
                .map(item -> unlocksMore(BuiltInRegistries.ITEM.getKey(item)))
                .orElse(false);
    }

    public static boolean unlocksMore(ResourceLocation itemId)
    {
        return ClientBookUpgrade.hintsUnlocked() && unlockingItems.contains(itemId);
    }

    // Whether an item may be named outside the book: Ponder's scenes ask this. Fails open while
    // the index is still empty: a screen opened before JEI has built it must show everything rather
    // than pretend the game is empty.
    public static boolean isDiscovered(ResourceLocation itemId)
    {
        if (!rules.enabled() || index.isEmpty())
            return true;
        return Unlocks.discovered(unlockedRegistryKeys, obtained(), itemId);
    }

    // Whether Ponder may show this item's scenes, which is a narrower question than isDiscovered:
    // the book shows recipes for things the player has not made yet, but a tour of a machine waits
    // until they have held one.
    public static boolean canPonder(ResourceLocation itemId)
    {
        return !rules.enabled() || obtained().contains(itemId);
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
    // count as unlocked: hiding a recipe we could not read would make it unreachable forever.
    public static boolean isRecipeUnlocked(String categoryUid, Object recipe)
    {
        if (!rules.enabled())
            return true;
        RecipeNode node = index.node(categoryUid, recipe);
        return node == null || Unlocks.recipeUnlocked(rules, index, node, obtained(), available);
    }

    // The group of variants an entry belongs to, or null when it stands alone.
    public static String variantGroup(BookIngredient ingredient)
    {
        return variantGroups.get(ingredient.unlockKey());
    }

    public static Set<String> outputsUsing(Set<String> inputKeys)
    {
        return Unlocks.outputsUsing(rules, index, inputKeys, obtained(), available);
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
        variantGroups = Map.of();
        unlockedOutputs = Set.of();
        unlockedRegistryKeys = Set.of();
        available = Set.of();
        heldVariantsSize = -1;
        unlockingItems = Set.of();
        obtainedSize = -1;
        newlyUnlocked.clear();
        highlighted.clear();
        seeded = false;
    }

    private static Set<ResourceLocation> obtained()
    {
        return ClientObtainedItems.current();
    }

    private Progression() {}
}
