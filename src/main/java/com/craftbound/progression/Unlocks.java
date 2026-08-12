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

    private Unlocks() {}
}
