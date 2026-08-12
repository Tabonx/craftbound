package com.craftbound.progression;

import java.util.HashSet;
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
            Set<ResourceLocation> obtained)
    {
        if (!rules.enabled())
            return true;
        return categoryUnlocked(rules, node.categoryUid(), index.catalystsFor(node.categoryUid()), obtained)
                && inputsSatisfied(rules, node.inputSlots(), obtained);
    }

    // A recipe with no judgeable slots is always satisfied: there is nothing to hold it back, and
    // refusing to show it would strand recipes whose inputs are all fluids.
    public static boolean inputsSatisfied(ProgressionRules rules, List<Set<ResourceLocation>> inputSlots,
            Set<ResourceLocation> obtained)
    {
        boolean anySatisfied = false;
        boolean anyJudgeable = false;
        for (Set<ResourceLocation> slot : inputSlots)
        {
            if (slot.isEmpty())
                continue;
            anyJudgeable = true;
            if (slot.stream().anyMatch(obtained::contains))
                anySatisfied = true;
            else if (rules.rule() == UnlockRule.ALL_INPUTS)
                return false;
        }
        return !anyJudgeable || anySatisfied;
    }

    // The output keys of every unlocked recipe: what the browse grid is allowed to show.
    public static Set<String> unlockedOutputs(ProgressionRules rules, RecipeIndex index,
            Set<ResourceLocation> obtained)
    {
        Set<String> unlocked = new HashSet<>();
        index.nodes()
                .filter(node -> recipeUnlocked(rules, index, node, obtained))
                .forEach(node -> unlocked.addAll(node.outputKeys()));
        return unlocked;
    }

    private Unlocks() {}
}
