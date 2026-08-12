package com.craftbound.client.progression;

import java.util.Set;

import com.craftbound.CraftboundAttachments;
import com.craftbound.client.jei.BookIngredient;
import com.craftbound.client.jei.CraftboundJeiPlugin;
import com.craftbound.progression.ProgressionConfig;
import com.craftbound.progression.ProgressionRules;
import com.craftbound.progression.RecipeIndex;
import com.craftbound.progression.RecipeNode;
import com.craftbound.progression.Unlocks;
import com.mojang.logging.LogUtils;

import org.slf4j.Logger;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;

// Client-side view of what the player has unlocked. The obtained set arrives on its own as a synced
// attachment, so nothing here talks to the server; it only caches the expensive parts — the recipe
// index and the derived unlocked-output set — and rebuilds them when the inputs change.
//
// Staleness is judged by the obtained set's size: it only ever grows, so a changed size is exactly
// a changed set, and comparing sizes costs nothing per frame.
public final class Progression
{
    private static final Logger LOGGER = LogUtils.getLogger();

    private static RecipeIndex index = RecipeIndex.EMPTY;
    private static ProgressionRules rules = ProgressionRules.OPEN;
    private static Set<String> unlockedOutputs = Set.of();
    private static int obtainedSize = -1;

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
            index = CraftboundJeiPlugin.buildRecipeIndex();

        rules = current;
        obtainedSize = obtained.size();
        unlockedOutputs = rules.enabled() ? Unlocks.unlockedOutputs(rules, index, obtained) : Set.of();

        LOGGER.debug("Progression: {} categories, {} recipes, {} items obtained, {} outputs unlocked",
                index.byCategory().size(), index.nodes().count(), obtainedSize, unlockedOutputs.size());
        return true;
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
        return node == null || Unlocks.recipeUnlocked(rules, index, node, obtained());
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
        index = RecipeIndex.EMPTY;
        unlockedOutputs = Set.of();
        obtainedSize = -1;
    }

    private static Set<ResourceLocation> obtained()
    {
        LocalPlayer player = Minecraft.getInstance().player;
        return player == null ? Set.of() : player.getData(CraftboundAttachments.OBTAINED_ITEMS);
    }

    private Progression() {}
}
