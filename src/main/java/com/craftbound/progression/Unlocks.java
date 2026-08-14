package com.craftbound.progression;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.minecraft.resources.ResourceLocation;

// Pure progression logic: what the player has ever obtained decides what the book shows. Kept free
// of Minecraft singletons and of JEI so it can be unit-tested without launching the game.
//
// Unlock state is derived, never stored: the obtained set only ever grows, so anything unlocked
// stays unlocked without a second thing to persist and keep in step.
public final class Unlocks
{
    public static boolean categoryUnlocked(ProgressionRules rules, String categoryUid,
            Set<ResourceLocation> catalysts, Set<ResourceLocation> obtained)
    {
        if (!rules.enabled() || !rules.gateCategories())
            return true;
        if (rules.exemptCategories().contains(categoryUid) || catalysts.isEmpty())
            return true;
        return catalysts.stream().anyMatch(obtained::contains);
    }

    public static boolean recipeUnlocked(ProgressionRules rules, RecipeIndex index, RecipeNode node,
            Set<ResourceLocation> obtained, Set<String> unlockedOutputs)
    {
        if (!rules.enabled())
            return true;
        return categoryUnlocked(rules, node.categoryUid(), index.catalystsFor(node.categoryUid()), obtained)
                && inputsSatisfied(rules, node.inputSlots(), obtained, unlockedOutputs, index.producedKeys());
    }

    // A recipe with no judgeable slots is always satisfied: there is nothing to hold it back, and
    // refusing to show it would strand recipes whose inputs we cannot read.
    public static boolean inputsSatisfied(ProgressionRules rules, List<InputSlot> inputSlots,
            Set<ResourceLocation> obtained, Set<String> unlockedOutputs, Set<String> producedKeys)
    {
        boolean anySatisfied = false;
        boolean anyJudgeable = false;
        for (InputSlot slot : inputSlots)
        {
            if (!judgeable(slot, producedKeys))
                continue;

            anyJudgeable = true;
            if (satisfied(slot, obtained, unlockedOutputs))
                anySatisfied = true;
            else if (rules.rule() == UnlockRule.ALL_INPUTS)
                return false;
        }
        return !anyJudgeable || anySatisfied;
    }

    private static boolean satisfied(InputSlot slot, Set<ResourceLocation> obtained,
            Set<String> unlockedOutputs)
    {
        return slot.items().stream().anyMatch(obtained::contains)
                || slot.fluids().stream().anyMatch(unlockedOutputs::contains);
    }

    // A slot demanding fluids is judged on those fluids, so a bucket sitting in `items` can help
    // satisfy the slot without turning an ungateable one (water) into a gate. Everything else is
    // judged on its items.
    private static boolean judgeable(InputSlot slot, Set<String> producedKeys)
    {
        return slot.fluids().isEmpty()
                ? !slot.items().isEmpty()
                : slot.fluids().stream().anyMatch(producedKeys::contains);
    }

    // Items whose absence is the only thing still holding a recipe back: obtaining one reveals
    // something the book is not showing yet. This is what the grid marks, rather than "never
    // obtained": a plank nobody has held is only worth marking while it still opens a door, so the
    // mark leaves every plank at once as soon as one of them satisfies the recipes they share.
    //
    // Judged one recipe ahead, not to a fixpoint: a mark promises the next step, not the whole tree.
    public static Set<ResourceLocation> unlockingItems(ProgressionRules rules, RecipeIndex index,
            Set<ResourceLocation> obtained, Set<String> unlockedOutputs)
    {
        if (!rules.enabled())
            return Set.of();

        Set<ResourceLocation> unlocking = new HashSet<>();
        Set<ResourceLocation> probe = new HashSet<>(obtained);
        index.nodes()
                .filter(node -> revealsSomething(node, unlockedOutputs))
                .filter(node -> !recipeUnlocked(rules, index, node, obtained, unlockedOutputs))
                .forEach(node -> {
                    for (ResourceLocation candidate : candidates(index, node))
                    {
                        if (obtained.contains(candidate) || unlocking.contains(candidate))
                            continue;

                        probe.add(candidate);
                        if (recipeUnlocked(rules, index, node, probe, unlockedOutputs))
                            unlocking.add(candidate);
                        probe.remove(candidate);
                    }
                });
        return unlocking;
    }

    // A locked recipe whose outputs are all reachable some other way adds nothing to the book.
    private static boolean revealsSomething(RecipeNode node, Set<String> unlockedOutputs)
    {
        return node.outputKeys().stream().anyMatch(key -> !unlockedOutputs.contains(key));
    }

    // Everything that could conceivably flip this recipe: its category's machines and whatever its
    // slots accept. Each is then tested for real, so entries that cannot help are dropped.
    private static Set<ResourceLocation> candidates(RecipeIndex index, RecipeNode node)
    {
        Set<ResourceLocation> candidates = new HashSet<>(index.catalystsFor(node.categoryUid()));
        node.inputSlots().forEach(slot -> candidates.addAll(slot.items()));
        return candidates;
    }

    // The output keys of every unlocked recipe: what the browse grid is allowed to show.
    //
    // Grown to a fixpoint rather than decided in one pass, because unlocking a recipe can unlock
    // another: chocolate becomes reachable the moment sugar and cocoa are in hand, and the Bar of
    // Chocolate that needs it becomes reachable in the same breath.
    public static Set<String> unlockedOutputs(ProgressionRules rules, RecipeIndex index,
            Set<ResourceLocation> obtained)
    {
        Set<String> unlocked = new HashSet<>();
        List<RecipeNode> pending = new ArrayList<>(index.nodes().toList());

        boolean grew = true;
        while (grew)
        {
            grew = false;
            Iterator<RecipeNode> remaining = pending.iterator();
            while (remaining.hasNext())
            {
                RecipeNode node = remaining.next();
                if (!recipeUnlocked(rules, index, node, obtained, unlocked))
                    continue;

                unlocked.addAll(node.outputKeys());
                remaining.remove();
                grew = true;
            }
        }
        return unlocked;
    }

    // Whether the player is allowed to know this item exists: its recipe is unlocked, or they have
    // held one. Holding it matters on its own because plenty of things no recipe produces, a Bell or
    // Create's creative-only blocks, can still be come by, and starting the game able to name them
    // gives away the whole catalogue.
    public static boolean discovered(Set<String> unlockedOutputs, Set<ResourceLocation> obtained,
            ResourceLocation itemId)
    {
        return obtained.contains(itemId) || unlockedOutputs.contains(UnlockKey.ofItem(itemId));
    }

    private Unlocks() {}
}
