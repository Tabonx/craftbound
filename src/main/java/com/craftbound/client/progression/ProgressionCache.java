package com.craftbound.client.progression;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.craftbound.client.jei.RecipeGate;
import com.craftbound.progression.ProgressionRules;
import com.craftbound.progression.RecipeIndex;
import com.craftbound.progression.RecipeNode;
import com.craftbound.progression.UnlockKey;
import com.craftbound.progression.Unlocks;
import com.craftbound.progression.VariantGroups;

import net.minecraft.resources.ResourceLocation;

// Any number of readers poll it. Each keeps the last version() it acted on and compares, so none of
// them can take a change away from another.
public final class ProgressionCache implements RecipeGate
{
    private final Supplier<Optional<Set<ResourceLocation>>> obtainedSource;
    private final Supplier<Set<UnlockKey>> heldVariantsSource;
    private final Supplier<ProgressionRules> rulesSource;
    private final Supplier<Optional<RecipeIndex>> indexSource;

    private final Watched<ResourceLocation> obtained = new Watched<>();
    private final Watched<UnlockKey> heldVariants = new Watched<>();
    private ProgressionRules rules = ProgressionRules.OPEN;
    private RecipeIndex index = RecipeIndex.EMPTY;
    private boolean ready = false;
    private int version = 0;

    private Map<UnlockKey, String> variantGroups = Map.of();
    private Set<UnlockKey> unlockedOutputs = Set.of();
    // The same with subtypes dropped, for questions asked of a whole item: one unlocked potion is
    // enough to know potions exist.
    private Set<UnlockKey> unlockedRegistryKeys = Set.of();
    // What recipe inputs are checked against: the unlocked outputs plus the variants held, so a held
    // water bottle satisfies the slots that ask for one without giving it an entry of its own.
    private Set<UnlockKey> available = Set.of();
    private Set<ResourceLocation> unlockingItems = Set.of();

    private final Set<UnlockKey> newlyUnlocked = new LinkedHashSet<>();
    private boolean baselined = false;

    // Kept apart from newlyUnlocked, which the toast drains as soon as it fires; this one survives
    // until the grid shows the entry, the way vanilla holds a highlight until the recipe appears.
    private final Set<UnlockKey> highlighted = new LinkedHashSet<>();

    public ProgressionCache(Supplier<Optional<Set<ResourceLocation>>> obtained, Supplier<Set<UnlockKey>> heldVariants,
            Supplier<ProgressionRules> rules, Supplier<Optional<RecipeIndex>> index)
    {
        this.obtainedSource = obtained;
        this.heldVariantsSource = heldVariants;
        this.rulesSource = rules;
        this.indexSource = index;
    }

    // Until both the recipe index and the obtained set are in, nothing counts as newly unlocked.
    public boolean ready()
    {
        return ready;
    }

    public int version()
    {
        return version;
    }

    public void refresh()
    {
        Optional<Set<ResourceLocation>> obtainedNow = obtainedSource.get();
        Optional<RecipeIndex> indexNow = indexSource.get();
        ProgressionRules rulesNow = rulesSource.get();
        boolean readyNow = obtainedNow.isPresent() && indexNow.isPresent();
        RecipeIndex indexOrEmpty = indexNow.orElse(RecipeIndex.EMPTY);
        boolean indexChanged = indexOrEmpty != index;

        boolean changed = obtained.update(obtainedNow.orElse(Set.of()))
                | heldVariants.update(heldVariantsSource.get())
                | !rulesNow.equals(rules)
                | indexChanged
                | readyNow != ready;
        if (!changed)
            return;

        rules = rulesNow;
        index = indexOrEmpty;
        ready = readyNow;
        if (indexChanged)
        {
            variantGroups = VariantGroups.of(index);
            highlighted.clear();
        }

        Set<UnlockKey> previous = unlockedOutputs;
        derive();
        version++;
        announce(previous, indexChanged);
    }

    private void derive()
    {
        unlockedOutputs = rules.enabled()
                ? Unlocks.unlockedOutputs(rules, index, obtained.value(), heldVariants.value())
                : Set.of();
        unlockedRegistryKeys = unlockedOutputs.stream().map(UnlockKey::withoutSubtype).collect(Collectors.toSet());
        available = new HashSet<>(unlockedOutputs);
        available.addAll(heldVariants.value());
        unlockingItems = Unlocks.unlockingItems(rules, index, obtained.value(), available);
    }

    // The first state with both sources in is what the player already had; announcing all of it
    // would bury them in toasts on every world join, and so would a new index after JEI reloads.
    // Only what unlocks afterwards is news.
    private void announce(Set<UnlockKey> previous, boolean indexChanged)
    {
        if (!ready || indexChanged)
        {
            baselined = false;
            newlyUnlocked.clear();
        }
        if (!ready)
            return;
        if (baselined)
        {
            for (UnlockKey key : unlockedOutputs)
            {
                if (!previous.contains(key))
                {
                    newlyUnlocked.add(key);
                    highlighted.add(key);
                }
            }
        }
        baselined = true;
    }

    public List<UnlockKey> drainNewlyUnlocked()
    {
        List<UnlockKey> keys = List.copyOf(newlyUnlocked);
        newlyUnlocked.clear();
        return keys;
    }

    // True once, for the first render that shows the entry.
    public boolean takeHighlight(UnlockKey unlockKey)
    {
        return highlighted.remove(unlockKey);
    }

    public boolean unlocksMore(ResourceLocation itemId)
    {
        return unlockingItems.contains(itemId);
    }

    // Fails open while the index is still empty: a screen opened before JEI has built it must show
    // everything rather than pretend the game is empty.
    public boolean isDiscovered(ResourceLocation itemId)
    {
        if (!rules.enabled() || index.isEmpty())
            return true;
        return Unlocks.discovered(unlockedRegistryKeys, obtainedNow(), itemId);
    }

    // Narrower than isDiscovered: the book shows recipes for things the player has not made yet, but
    // a tour of a machine waits until they have held one.
    public boolean canPonder(ResourceLocation itemId)
    {
        return !rules.enabled() || obtainedNow().contains(itemId);
    }

    public boolean isUnlocked(UnlockKey unlockKey)
    {
        return !rules.enabled() || unlockedOutputs.contains(unlockKey);
    }

    // Judged within the category being shown: the same recipe object is listed by several
    // categories, and being locked as a mechanical-crafter recipe says nothing about it as a
    // crafting-table one. Recipes missing from the index (a category that failed to lay one out)
    // count as unlocked: hiding a recipe we could not read would make it unreachable forever.
    @Override
    public boolean isRecipeUnlocked(String categoryUid, Object recipe)
    {
        if (!rules.enabled())
            return true;
        RecipeNode node = index.node(categoryUid, recipe);
        return node == null || Unlocks.recipeUnlocked(rules, index, node, obtainedNow(), available);
    }

    @Override
    public boolean isCategoryUnlocked(String categoryUid)
    {
        if (!rules.enabled())
            return true;
        return Unlocks.categoryUnlocked(rules, categoryUid, index.catalystsFor(categoryUid), obtainedNow());
    }

    @Override
    public boolean narrowsSlots()
    {
        return rules.enabled();
    }

    // Anything that is not an item, such as a fluid, is never hidden.
    @Override
    public boolean hasInput(UnlockKey unlockKey, Optional<ResourceLocation> itemId)
    {
        if (itemId.isEmpty())
            return true;
        return unlockKey.hasSubtype()
                ? available.contains(unlockKey)
                : obtainedNow().contains(itemId.get());
    }

    // Null when the entry stands alone.
    public String variantGroup(UnlockKey unlockKey)
    {
        return variantGroups.get(unlockKey);
    }

    public Set<UnlockKey> outputsUsing(Set<UnlockKey> inputKeys)
    {
        return Unlocks.outputsUsing(rules, index, inputKeys, obtainedNow(), available);
    }

    // Questions read the obtained set live, so an item picked up since the last refresh already
    // counts, and Ponder gets an answer before JEI has started.
    private Set<ResourceLocation> obtainedNow()
    {
        return obtainedSource.get().orElse(Set.of());
    }

    // A set some source hands out, which it may replace or grow in place. Checking the reference and
    // the size first keeps an unchanged set free to poll every frame; a set that moved is then
    // compared by content, so a world whose set happens to be just as large still counts as new.
    private static final class Watched<E>
    {
        private Set<E> value = Set.of();
        private int size = 0;
        private Set<E> seen = Set.of();

        boolean update(Set<E> current)
        {
            if (current == value && current.size() == size)
                return false;
            value = current;
            size = current.size();
            if (current.equals(seen))
                return false;
            seen = Set.copyOf(current);
            return true;
        }

        Set<E> value()
        {
            return value;
        }
    }
}
